/*
 * Created by YaMing Wu 2019-01-02
 */

package rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HelloService extends Remote {
    String sayHello(String someOne) throws RemoteException;
}
