package yaming.rpc.framework.helper;

import yaming.rpc.framework.serialization.common.SerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertyConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyConfigHelper.class);

    private static final String PROPERTY_CLASSPATH = "/yaming_rpc.properties";
    private static final Properties properties = new Properties();

    // zookeeper server address
    private static String zkService = "";
    // zookeeper session timeout value
    private static int zkSessionTimeout;
    // zookeeper connection timeout value
    private static int zkConnectionTimeout;
    // type of serializer
    private static SerializerType serializerType;
    // number of netty connections provided by each service provider
    private static int channelConnectSize;


    /**
     * init
     */
    static {
        InputStream is = null;
        try {
            is = PropertyConfigHelper.class.getResourceAsStream(PROPERTY_CLASSPATH);
            if (null == is) {
                throw new IllegalStateException("ares_remoting.properties can not found in the classpath.");
            }
            properties.load(is);

            zkService = properties.getProperty("zk_service");
            zkSessionTimeout = Integer.parseInt(properties.getProperty("zk_sessionTimeout", "500"));
            zkConnectionTimeout = Integer.parseInt(properties.getProperty("zk_connectionTimeout", "500"));
            channelConnectSize = Integer.parseInt(properties.getProperty("channel_connect_size", "10"));
            String seriType = properties.getProperty("serialize_type");
            serializerType = SerializerType.queryByType(seriType);
            if (serializerType == null) {
                throw new RuntimeException("serializerType is null");
            }

        } catch (Throwable t) {
            logger.warn("load ares_remoting's properties file failed.", t);
            throw new RuntimeException(t);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String getZkService() {
        return zkService;
    }

    public static int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public static int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public static int getChannelConnectSize() {
        return channelConnectSize;
    }

    public static SerializerType getSerializerType() {
        return serializerType;
    }
}
