package http.invoker.rpc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import rpc.common.User;
import rpc.common.UserService;

public class HttpInvokeClient {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("httpinvoker-rpc-client.xml");
        UserService userService = (UserService) context.getBean("userServiceProxy");

        User user = userService.findByName("user1");
        System.out.println(user.getName() + " " + user.getEmail());
    }
}
