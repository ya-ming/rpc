package yaming.rpc.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainServer {


    public static void main(String[] args) throws Exception {

        // publish the service
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rpc-server.xml");
        System.out.println("Service Published");
    }
}
