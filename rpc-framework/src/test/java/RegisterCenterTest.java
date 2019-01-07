import com.google.common.collect.Lists;
import yaming.rpc.framework.helper.IPHelper;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.Method;
import java.util.List;

public class RegisterCenterTest {
    public static void main(String[] args) throws InterruptedException {
        RegisterCenter registerCenter = RegisterCenter.singleton();

        registerCenter.registerProvider(buildProviderServiceInfos(IServiceAForTest.class, 1));
        registerCenter.registerProvider(buildProviderServiceInfos(IServiceBForTest.class, 2));
        Thread.sleep(Integer.MAX_VALUE);
    }

    private static List<ProviderService> buildProviderServiceInfos(Class<?> clazz, int id) {
        List<ProviderService> providerList = Lists.newArrayList();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            ProviderService providerService = new ProviderService();
            providerService.setServiceItf(clazz);
            providerService.setServiceObject(clazz);
            providerService.setServerIp(IPHelper.localIp());
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
