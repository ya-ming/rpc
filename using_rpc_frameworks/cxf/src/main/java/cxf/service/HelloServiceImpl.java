/*
 * Created by YaMing Wu 2019-01-02
 */

package cxf.service;

import javax.jws.WebService;

@WebService(endpointInterface = "cxf.service.HelloService")
public class HelloServiceImpl implements HelloService {

    public String sayHello(String somebody) {
        return "Hello, " + somebody;
    }
}
