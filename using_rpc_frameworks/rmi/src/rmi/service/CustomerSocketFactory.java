/*
 * Created by YaMing Wu 2019-01-02
 */

package rmi.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class CustomerSocketFactory extends RMISocketFactory {
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        if (port == 0) {
            port = 8501;
        }

        System.out.println("RMI notify port: " + port);
        return new ServerSocket(port);
    }
}
