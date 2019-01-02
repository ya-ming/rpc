/*
 * Created by YaMing Wu 2019-01-02
 */

package rmi.service;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        // register the service
        LocateRegistry.createRegistry(8801);

        // using socket factory to specify the communication port
        RMISocketFactory.setSocketFactory(new CustomerSocketFactory());

        // create the service
        HelloService helloService = new HelloServiceImpl();


        Naming.bind("rmi://localhost:8801/helloService", helloService);
        System.out.println("ServerMain provide RPC service now");
    }
}
