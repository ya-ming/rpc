package yaming.rpc.framework.zookeeper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.helper.PropertyConfigHelper;
import yaming.rpc.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

public class RegisterCenter implements IRegistercenter4Provider{
    private static RegisterCenter registerCenter = new RegisterCenter();

    // list of service provider
    // key: interface of the service provider
    // value: method list of the service provider
    private static final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();

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
                        // refreshActivityService(activityServiceIpList);
                    }
                });
            }
        }
//        System.out.println("registerProvider done");
    }

    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return providerServiceMap;
    }
}
