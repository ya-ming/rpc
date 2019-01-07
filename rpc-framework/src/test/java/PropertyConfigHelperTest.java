import yaming.rpc.framework.helper.PropertyConfigHelper;

public class PropertyConfigHelperTest {
    public static void main(String[] args) {
        System.out.println("zookeeper server address: " + PropertyConfigHelper.getZkService());
        System.out.println("zookeeper session timeout value: " + PropertyConfigHelper.getZkSessionTimeout());
        System.out.println("zookeeper connection timeout value: " + PropertyConfigHelper.getZkConnectionTimeout());
        System.out.println("number of netty connections provided by each service provider: " + PropertyConfigHelper.getChannelConnectSize());
        System.out.println("type of serializer: " + PropertyConfigHelper.getSerializeType());
    }
}
