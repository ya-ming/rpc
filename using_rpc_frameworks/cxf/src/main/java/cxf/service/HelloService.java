/*
 * Created by YaMing Wu 2019-01-02
 */

package cxf.service;

import javax.jws.WebService;

@WebService
public interface HelloService {
    public String sayHello(String somebody);
}
