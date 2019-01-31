package yaming.rpc.framework.provider;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Provider;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * factory bean of the service provider
 */
public class ProviderFactoryBean implements FactoryBean, InitializingBean {

    // service interface
    private Class<?> serviceItf;
    // realization of the service
    private Object serviceObject;
    // service port
    private String serverPort;
    // timeout time
    private long timeout;
    // proxy object of the service, not in use
    private Object serviceProxyObject;
    // unique id of the service provider
    private String appKey;
    // service group name
    private String groupName = "default";
    // weight of the service provider, default is 1, can be [1 - 100]
    private int weight = 1;
    // number of threads on the server side, default is 10
    private int workerThreads = 10;

    @Override
    public Object getObject() throws Exception {
        return serviceProxyObject;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceItf;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        // start the Netty Server
        NettyServer.singleton().start(Integer.parseInt(serverPort));

        // register tot he ZooKeeper
        List<ProviderService> providerServiceList = buildProviderServiceInfos();
        IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
        registerCenter4Provider.registerProvider(providerServiceList);
    }


    private List<ProviderService> buildProviderServiceInfos() {
        List<ProviderService> providerList = Lists.newArrayList();
        Method[] methods = serviceObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            ProviderService providerService = new ProviderService();
            providerService.setServiceItf(serviceItf);
            providerService.setServiceObject(serviceObject);
            providerService.setServerIp(IPHelper.localIp());
            providerService.setServerPort(Integer.parseInt(serverPort));
            providerService.setTimeout(timeout);
            providerService.setServiceMethod(method);
            providerService.setWeight(weight);
            providerService.setWorkerThreads(workerThreads);
            providerService.setAppKey(appKey);
            providerService.setGroupName(groupName);
            providerList.add(providerService);
        }
        return providerList;
    }


    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public void setServiceItf(Class<?> serviceItf) {
        this.serviceItf = serviceItf;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Object getServiceProxyObject() {
        return serviceProxyObject;
    }

    public void setServiceProxyObject(Object serviceProxyObject) {
        this.serviceProxyObject = serviceProxyObject;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
