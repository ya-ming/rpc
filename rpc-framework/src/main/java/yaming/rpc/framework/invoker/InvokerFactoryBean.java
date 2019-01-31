package yaming.rpc.framework.invoker;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Invoker;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.util.List;
import java.util.Map;

/**
 * factory bean of the invoker
 */
public class InvokerFactoryBean implements FactoryBean, InitializingBean {

    // service interface
    private Class<?> targetInterface;
    // timeout time
    private int timeout;
    // service bean
    private Object serviceObject;
    // strategy of loading balance
    private String clusterStrategy;
    // the unique id of the service provider
    private String remoteAppKey;
    // group name of the service
    private String groupName = "default";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }


    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // connect to the register center
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        // init the list of service provider and save to local cache
        registerCenter4Consumer.initProviderMap(remoteAppKey, groupName);

        // init Netty Channel
        Map<String, List<ProviderService>> providerMap = registerCenter4Consumer.getServiceMetaDataMap4Consumer();
        if (MapUtils.isEmpty(providerMap)) {
            throw new RuntimeException("service provider list is empty.");
        }
        NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(providerMap);

        // get the proxy of the service provider
        InvokerProxyBeanFactory proxyFactory = InvokerProxyBeanFactory.singleton(targetInterface, timeout, clusterStrategy);
        this.serviceObject = proxyFactory.getProxy();

        // register the consumer to the register center
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(targetInterface);
        invoker.setRemoteAppKey(remoteAppKey);
        invoker.setGroupName(groupName);
        registerCenter4Consumer.registerInvoker(invoker);
    }


    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
