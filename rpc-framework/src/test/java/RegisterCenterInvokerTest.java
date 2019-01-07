import com.google.common.collect.Lists;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Invoker;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RegisterCenterInvokerTest {
    public static void main(String[] args) throws InterruptedException {
        // test the consumer interface
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        registerCenter4Consumer.initProviderMap("appKey-1", "default");
        Map<String, List<ProviderService>> serviceMetaDataMap4Consumer = registerCenter4Consumer.getServiceMetaDataMap4Consumer();
        System.out.println(serviceMetaDataMap4Consumer);

        registerCenter4Consumer.registerInvoker(buildInvokerServiceInfo(IServiceAForTest.class));

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
