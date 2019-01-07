package yaming.rpc.framework.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class IPHelper {

    private static final Logger logger = LoggerFactory.getLogger(IPHelper.class);

    private static String hostIp = StringUtils.EMPTY;


    /**
     * get local host IP
     * iterate all the interfaceAddress of all the networkInterface
     * return IP v4 address if the interfaceAddress is <code>InetAddress instanceof Inet4Address</code>
     *
     * @return
     */
    public static String localIp() {
        return hostIp;
    }


    public static String getRealIp() {
        String localip = null;// local IP, return it if no public IP found
        String netip = null;// public IP

        try {
            Enumeration<NetworkInterface> netInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {// public IP
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()
                            && !ip.getHostAddress().contains(":")) {// local IP
                        localip = ip.getHostAddress();
                    }
                }
            }

            if (netip != null && !"".equals(netip)) {
                return netip;
            } else {
                return localip;
            }
        } catch (SocketException e) {
            logger.warn("Failed to get the Host IP" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    static {

        String ip = null;
        Enumeration allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                List<InterfaceAddress> InterfaceAddress = netInterface.getInterfaceAddresses();
                for (InterfaceAddress add : InterfaceAddress) {
                    InetAddress Ip = add.getAddress();
                    if (Ip != null && Ip instanceof Inet4Address) {
                        if (StringUtils.equals(Ip.getHostAddress(), "127.0.0.1")) {
                            continue;
                        }
                        ip = Ip.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            logger.warn("Failed to get the Host IP:" + e.getMessage());
            throw new RuntimeException(e);
        }
        hostIp = ip;
    }


    /**
     * return the first valid IP of the host
     * return null if no IP available
     *
     * @return
     */
    public static String getHostFirstIp() {
        return hostIp;
    }
}
