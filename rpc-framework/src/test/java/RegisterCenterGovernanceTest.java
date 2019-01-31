import yaming.rpc.framework.zookeeper.RegisterCenter;

public class RegisterCenterGovernanceTest {
    public static void main(String[] args) throws InterruptedException {
        // yaming.rpc.framework.test the provider interface
        RegisterCenter registerCenter = RegisterCenter.singleton();
        System.out.println(registerCenter.queryProvidersAndInvokers(IServiceAForTest.class.getName(), "appKey-1"));
    }
}
