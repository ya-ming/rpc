package cxf.client;

import cxf.service.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfClient {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:cxf-client.xml");
        HelloService client = (HelloService) context.getBean("helloClient");
        System.out.println(client.sayHello("yaming"));
    }
}
