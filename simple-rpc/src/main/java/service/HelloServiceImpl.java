package service;

public class HelloServiceImpl implements HelloService {
    public String sayHello(String content) {
        return "Hello, " + content;
    }
}
