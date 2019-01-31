import com.google.common.collect.Lists;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Invoker;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RegisterCenterProviderTest {
    public static void main(String[] args) throws InterruptedException {
        // yaming.rpc.framework.test the provider interface
        RegisterCenter registerCenter = RegisterCenter.singleton();

        registerCenter.registerProvider(buildProviderServiceInfos(IServiceAForTest.class, 1));
        registerCenter.registerProvider(buildProviderServiceInfos(IServiceBForTest.class, 2));

        Map<String, List<ProviderService>> providerServiceMap = registerCenter.getProviderServiceMap();
        System.out.println(providerServiceMap);

        // sleep forever to keep the node in zookeeper alive
        Thread.sleep(Integer.MAX_VALUE);
    }

    private static List<ProviderService> buildProviderServiceInfos(Class<?> clazz, int id) {
        List<ProviderService> providerList = Lists.newArrayList();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            ProviderService providerService = new ProviderService();
            providerService.setServiceItf(clazz);
            providerService.setServiceObject(clazz);
            providerService.setServerIp("192.168.100." + id);
            providerService.setServerPort(Integer.parseInt("880" + id));
            providerService.setTimeout(3000);
            providerService.setServiceMethod(method);
            providerService.setWeight(1000);
            providerService.setWorkerThreads(1000);
            providerService.setAppKey("appKey-1");
            providerService.setGroupName("default");
            providerList.add(providerService);
        }
        return providerList;
    }
}
