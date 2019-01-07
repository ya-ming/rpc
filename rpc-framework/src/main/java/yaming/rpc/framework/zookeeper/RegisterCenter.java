package yaming.rpc.framework.zookeeper;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.helper.PropertyConfigHelper;
import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

public class RegisterCenter implements IRegisterCenter4Provider, IRegisterCenter4Invoker, IRegisterCenter4Governance{
    private static RegisterCenter registerCenter = new RegisterCenter();

    // list of service provider
    // key: interface of the service provider
    // value: method list of the service provider
    private static final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();
    // list of service provider, fetch from the server at the beginning, get updated upon notified
    // key: remote interface of the service provider
    // value: method list of the service provider
    private static final Map<String, List<ProviderService>> serviceMetaDataMap4Consumer = Maps.newConcurrentMap();


    private static String ZK_SERVICE = PropertyConfigHelper.getZkService();
    private static int ZK_SESSION_TIME_OUT = PropertyConfigHelper.getZkConnectionTimeout();
    private static int ZK_CONNECTION_TIME_OUT = PropertyConfigHelper.getZkConnectionTimeout();
    private static String ROOT_PATH = "/config_register";
    public static String PROVIDER_TYPE = "provider";
    public static String INVOKER_TYPE = "consumer";
    private static volatile ZkClient zkClient = null;

    private RegisterCenter() {

    }

    public static RegisterCenter singleton() {
        return registerCenter;
    }

    /**
     * implements the interfaces of IRegisterCenter4Provider
     */

    /**
     *
     * @param serviceMetaData
     */
    @Override
    public void registerProvider(List<ProviderService> serviceMetaData) {
//        System.out.println("registerProvider");

        if (CollectionUtils.isEmpty(serviceMetaData)) {
            return;
        }

        // connect to zookeeper and register the service
        synchronized (RegisterCenter.class) {
            for (ProviderService provider : serviceMetaData) {
                String serviceItfKey = provider.getServiceItf().getName();

                List<ProviderService> providers = providerServiceMap.get(serviceItfKey);
                if (providers == null) {
                    providers = Lists.newArrayList();
                }
                providers.add(provider);
                providerServiceMap.put(serviceItfKey, providers);
            }

            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            // create zookeeper namespace and app namespace
            String APP_KEY = serviceMetaData.get(0).getAppKey();
            String ZK_PATH = ROOT_PATH + "/" + APP_KEY;
            boolean exist = zkClient.exists(ZK_PATH);
            if (!exist) {
                zkClient.createPersistent(ZK_PATH, true);
            }

            for (Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()) {
                // create service provider
                String groupName = entry.getValue().get(0).getGroupName();
                String serviceNode = entry.getKey();
                String servicePath = ZK_PATH + "/" + groupName + "/" + serviceNode + "/" + PROVIDER_TYPE;
                exist = zkClient.exists(servicePath);
                if (!exist) {
                    zkClient.createPersistent(servicePath, true);
                }

                // create service node
                int serverPort = entry.getValue().get(0).getServerPort();
                int weigh = entry.getValue().get(0).getWeight();
                int workerThreads = entry.getValue().get(0).getWorkerThreads();
                String localIp = IPHelper.localIp();
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weigh + "|" + workerThreads + "|" + groupName;
                exist = zkClient.exists(currentServiceIpNode);
                if (!exist) {
                    zkClient.createEphemeral(currentServiceIpNode);
                }

                // subscribe to the change of the service and update the local cache
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        if (currentChilds == null) {
                            currentChilds = Lists.newArrayList();
                        }

                        // IP of the alive services
                        List<String> activityServiceIpList = Lists.newArrayList(Lists.transform(currentChilds, new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return StringUtils.split(input, "|")[0];
                            }
                        }));
                        refreshActivityService(activityServiceIpList);
                    }
                });
            }
        }
//        System.out.println("registerProvider done");
    }

    /**
     * utilize the auto refresh capability of zookeeper to refresh the list of active service provider
     * @param serviceIpList
     */
    private void refreshActivityService(List<String> serviceIpList) {
        if (serviceIpList == null) {
            serviceIpList = Lists.newArrayList();
        }

        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newHashMap();
        for (Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()) {
            String key = entry.getKey();
            List<ProviderService> providerServices = entry.getValue();

            List<ProviderService> serviceMetaDataModelList = currentServiceMetaDataMap.get(key);
            if (serviceMetaDataModelList == null) {
                serviceMetaDataModelList = Lists.newArrayList();
            }

            for (ProviderService serviceMetaData : providerServices) {
                if (serviceIpList.contains(serviceMetaData.getServerIp())) {
                    serviceMetaDataModelList.add(serviceMetaData);
                }
            }

            currentServiceMetaDataMap.put(key, serviceMetaDataModelList);
        }

        providerServiceMap.clear();
        System.out.println("refreshActivityService, " + JSON.toJSONString(currentServiceMetaDataMap));
        providerServiceMap.putAll(currentServiceMetaDataMap);
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return providerServiceMap;
    }


    /**
     * implements the interfaces of IRegisterCenter4Invoker
     */
    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {
        if (MapUtils.isEmpty(serviceMetaDataMap4Consumer)) {
            serviceMetaDataMap4Consumer.putAll(fetchOrUpdateServiceMetaData(remoteAppKey, groupName));
        }
    }

    private Map<String,List<ProviderService>> fetchOrUpdateServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            // fetch the list of service provider from the zookeeper
            String providerPath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName;
            List<String> providerServices = zkClient.getChildren(providerPath);

            for (String serviceName : providerServices) {
                String servicePath = providerPath + "/" + serviceName + "/" + PROVIDER_TYPE;
                List<String> ipPathList = zkClient.getChildren(servicePath);
                for (String ipPath : ipPathList) {
                    String[] itemList = StringUtils.split(ipPath, "|");
                    String serverIp = itemList[0];
                    int serverPort = Integer.parseInt(itemList[1]);
                    int weight = Integer.parseInt(itemList[2]);
                    int workerThreads = Integer.parseInt(itemList[3]);
                    String group = itemList[4];

                    List<ProviderService> providerServiceList = providerServiceMap.get(serviceName);
                    if (providerServiceList == null) {
                        providerServiceList = Lists.newArrayList();
                    }
                    ProviderService providerService = new ProviderService();

                    try {
                        providerService.setServiceItf(ClassUtils.getClass(serviceName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    providerService.setServerIp(serverIp);
                    providerService.setServerPort(serverPort);
                    providerService.setWeight(weight);
                    providerService.setWorkerThreads(workerThreads);
                    providerService.setGroupName(group);
                    providerServiceList.add(providerService);

                    providerServiceMap.put(serviceName, providerServiceList);
                }

                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        if (currentChilds == null) {
                            currentChilds = Lists.newArrayList();
                        }
                        currentChilds = Lists.newArrayList(Lists.transform(currentChilds, new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return StringUtils.split(input, "|")[0];
                            }
                        }));
                        refreshServiceMetaDataMap(currentChilds);
                    }
                });
            }
            return providerServiceMap;
        }
    }

    private void refreshServiceMetaDataMap(List<String> serviceIpList) {
        if (serviceIpList == null) {
            serviceIpList = Lists.newArrayList();
        }

        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newHashMap();
        for (Map.Entry<String, List<ProviderService>> entry : serviceMetaDataMap4Consumer.entrySet()) {
            String serviceItfKey = entry.getKey();
            List<ProviderService> serviceList = entry.getValue();

            List<ProviderService> providerServiceList = currentServiceMetaDataMap.get(serviceItfKey);
            if (providerServiceList == null) {
                providerServiceList = Lists.newArrayList();
            }

            for (ProviderService serviceMetaData : serviceList) {
                if (serviceIpList.contains(serviceMetaData.getServerIp())) {
                    providerServiceList.add(serviceMetaData);
                }
            }
            currentServiceMetaDataMap.put(serviceItfKey, providerServiceList);
        }

        serviceMetaDataMap4Consumer.clear();
        System.out.println("refreshServiceMetaDataMap, " + JSON.toJSONString(currentServiceMetaDataMap));
        serviceMetaDataMap4Consumer.putAll(currentServiceMetaDataMap);
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consumer() {
        return serviceMetaDataMap4Consumer;
    }

    /**
     *
     * @param invoker
     */
    @Override
    public void registerInvoker(InvokerService invoker) {
        if (invoker == null) {
            return;
        }

        // register the service in the zookeeper
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            // create the namespace
            boolean exist = zkClient.exists(ROOT_PATH);
            if (!exist) {
                zkClient.createPersistent(ROOT_PATH, true);
            }

            // create node for the consumer
            String remoteAppKey = invoker.getRemoteAppKey();
            String groupName = invoker.getGroupName();
            String serviceNode = invoker.getServiceItf().getName();
            String servicePath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName + "/" + serviceNode + "/" + INVOKER_TYPE;
            exist = zkClient.exists(servicePath);
            if (!exist) {
                zkClient.createPersistent(servicePath, true);
            }

            // create node for the current server
            String localIp = IPHelper.localIp();
            String currentServiceIpNode = servicePath + "/" + localIp;
            exist = zkClient.exists(currentServiceIpNode);
            if (!exist) {
                zkClient.createEphemeral(currentServiceIpNode);
            }
        }
    }

    @Override
    public Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey) {
        List<InvokerService> invokerServices = Lists.newArrayList();
        List<ProviderService> providerServices = Lists.newArrayList();

        if (zkClient == null) {
            synchronized (RegisterCenter.class) {
                if (zkClient == null) {
                    zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
                }
            }
        }

        // get list of ROOT_PATH/appKey
        String parentPath = ROOT_PATH + "/" + appKey;
        List<String> groupServiceList = zkClient.getChildren(parentPath);
        if (CollectionUtils.isEmpty(groupServiceList)) {
            return Pair.of(providerServices, invokerServices);
        }

        for (String group : groupServiceList) {
            // get list of ROOT_PATH/appKey/group
            String groupPath = parentPath + "/" + group;
            List<String> serviceList = zkClient.getChildren(groupPath);
            if (CollectionUtils.isEmpty(serviceList)) {
                continue;
            }

            for (String service : serviceList) {
                // get list of ROOT_PATH/appKey/group/service
                String servicePath = groupPath + "/" + service;
                List<String> serviceTypes = zkClient.getChildren(servicePath);
                if (CollectionUtils.isEmpty(serviceTypes)) {
                    continue;
                }

                for (String serviceType : serviceTypes) {
                    if (StringUtils.equals(serviceType, PROVIDER_TYPE)) {
                        // get list of ROOT_PATH/appKey/group/service/serviceType
                        String providerPath = servicePath + "/" + serviceType;
                        List<String> providers = zkClient.getChildren(providerPath);
                        if (CollectionUtils.isEmpty(providers)) {
                            continue;
                        }

                        // get information of the provider
                        for (String provider : providers) {
                            String[] providerNodeArr = StringUtils.split(provider, "|");

                            ProviderService providerService = new ProviderService();
                            providerService.setAppKey(appKey);
                            providerService.setGroupName(group);
                            providerService.setServerIp(providerNodeArr[0]);
                            providerService.setServerPort(Integer.parseInt(providerNodeArr[1]));
                            providerService.setWeight(Integer.parseInt(providerNodeArr[2]));
                            providerService.setWorkerThreads(Integer.parseInt(providerNodeArr[3]));
                            providerServices.add(providerService);
                        }
                    } else if (StringUtils.equals(serviceType, INVOKER_TYPE)) {
                        // get the list of  ROOT_PATH/appKey/group/service/serviceType
                        String invokerPath = servicePath + "/" + serviceType;
                        List<String> invokers = zkClient.getChildren(invokerPath);
                        if (CollectionUtils.isEmpty(invokers)) {
                            continue;
                        }

                        // get the information of the invokers
                        for (String invoker : invokers) {
                            InvokerService invokerService = new InvokerService();
                            invokerService.setRemoteAppKey(appKey);
                            invokerService.setGroupName(group);
                            invokerService.setInvokerIp(invoker);
                            invokerServices.add(invokerService);
                        }
                    }
                }
            }
        }

        return Pair.of(providerServices, invokerServices);
    }
}
