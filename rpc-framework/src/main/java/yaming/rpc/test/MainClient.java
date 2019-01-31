package yaming.rpc.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) throws Exception {

        // declare the remote service
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("rpc-client.xml");
        // get the remote service
        final HelloService helloService = (HelloService) context.getBean("remoteHelloService");

        long count = 1000000000000000000L;

        // call the RPC and print the result
        for (int i = 0; i < count; i++) {
            try {
                String result = helloService.sayHello("rpc-framework,i=" + i);
                System.out.println(result);
            } catch (Exception e) {
                logger.warn("--------", e);
            }
        }

        System.exit(0);
    }
}
