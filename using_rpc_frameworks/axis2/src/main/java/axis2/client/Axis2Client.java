package axis2.client;


import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.namespace.QName;

public class Axis2Client {
    public static void main(String[] args) {
        try {
            EndpointReference targetEPR = new EndpointReference("http://localhost:8081/services/HelloService");
            RPCServiceClient serviceClient = new RPCServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
            options.setTo(targetEPR);

            QName opQName = new QName("http://service.axis2", "sayHello");
            Object[] paramArgs = new Object[]{"yaming"};

            Class[] returnTypes = new Class[]{String.class};
            Object[] response = serviceClient.invokeBlocking(opQName, paramArgs, returnTypes);
            serviceClient.cleanupTransport();
            String result = (String) response[0];
            if (result == null) {
                System.out.println("HelloService didn't initialize!");
            } else {
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
