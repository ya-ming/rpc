/*
 * Created by YaMing Wu 2019-01-02
 */

package rmi.client;

import rmi.service.HelloService;

import java.rmi.Naming;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        HelloService helloService = (HelloService) Naming.lookup("rmi://localhost:8801/helloService");

        System.out.println("RMI service returns: " + helloService.sayHello("yaming"));
    }
}
