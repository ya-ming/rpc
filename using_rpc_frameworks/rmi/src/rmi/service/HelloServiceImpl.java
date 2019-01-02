/*
 * Created by YaMing Wu 2019-01-02
 */

package rmi.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {
    private static final long serialVersionUID = -6190513770400890033L;

    public HelloServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String sayHello(String someOne) throws RemoteException {
        return "Hello, " + someOne;
    }
}
