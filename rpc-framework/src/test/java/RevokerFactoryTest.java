import org.apache.commons.collections.MapUtils;
import yaming.rpc.framework.Revoker.NettyChannelPoolFactory;
import yaming.rpc.framework.Revoker.RevokerProxyBeanFactory;
import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Invoker;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.util.List;
import java.util.Map;

public class RevokerFactoryTest {
    public static void main(String[] args) throws Throwable {
        // test the consumer interface
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        registerCenter4Consumer.initProviderMap("appKey-1", "default");

        Map<String, List<ProviderService>> serviceMetaDataMap4Consumer = registerCenter4Consumer.getServiceMetaDataMap4Consumer();
        System.out.println(serviceMetaDataMap4Consumer);
        // Init Netty Channel
        if (MapUtils.isEmpty(serviceMetaDataMap4Consumer)) {
            throw new RuntimeException("service provider list is empty.");
        }
        NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(serviceMetaDataMap4Consumer);

        // get the proxy of service provider
        RevokerProxyBeanFactory proxyFactory = RevokerProxyBeanFactory.singleton(IServiceAForTest.class, 3000, "");
        Object serviceObject = proxyFactory.getProxy();

        registerCenter4Consumer.registerInvoker(buildInvokerServiceInfo(IServiceAForTest.class));

        Object result = proxyFactory.invoke(serviceObject, IServiceAForTest.class.getMethod("methodA"), null);
        System.out.println(result);

        result = proxyFactory.invoke(serviceObject, IServiceAForTest.class.getMethod("methodB", String.class), new Object[]{"this is a test"});
        System.out.println(result);


        // sleep forever to keep the node in zookeeper alive
        Thread.sleep(Integer.MAX_VALUE);
    }

    private static InvokerService buildInvokerServiceInfo(Class<?> clazz) {
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(clazz);
        invoker.setRemoteAppKey("appKey-1");
        invoker.setGroupName("default");
        return invoker;
    }
}
