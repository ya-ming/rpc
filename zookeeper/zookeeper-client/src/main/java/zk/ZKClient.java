package zk;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

public class ZKClient {
    public static void main(String[] args) throws InterruptedException {
        String zkServer = "127.0.0.1:2181";
        int connectionTimeout = 3000;
        ZkClient zkClient = new ZkClient(zkServer, connectionTimeout);

        String registryPath = "/registry";
        String serviceName = "nullService";
        String servicePath = registryPath + "/" + serviceName;
        String addressPath = servicePath + "/address-";

        boolean cleanup = false;

        // retrieve client id and clean up flag
        int clientId = 0;
        if (args.length == 2) {
            cleanup = Boolean.parseBoolean(args[1]);
        }
        if (args.length >= 1) {
            clientId = Integer.parseInt(args[0]);
        }

        if (cleanup) {
            if (zkClient.exists(registryPath)) {
                zkClient.delete(registryPath);
            }
            if (zkClient.exists(servicePath)) {
                zkClient.delete(servicePath);
            }
        }

        if (zkClient.exists(registryPath) == false) {
            zkClient.createPersistent(registryPath);
        }
        if (zkClient.exists(servicePath) == false) {
            zkClient.createPersistent(servicePath);
        }
        zkClient.createEphemeralSequential(addressPath, "127.0.0.1:880" + clientId);

        String data = zkClient.<String>readData(registryPath, true);
        System.out.println("Data: " + data);

        // register to the server and listen to changes on the registryPath
        zkClient.subscribeDataChanges(registryPath, new IZkDataListener() {
            public void handleDataChange(String s, Object o) throws Exception {
                System.out.println("handleDataChange, dataPath: " + s + ", data: " + o);
            }

            public void handleDataDeleted(String s) throws Exception {
                System.out.println("handleDataChange, dataPath: " + s);
            }
        });

        int i = 0;
        while (true) {
            zkClient.writeData(registryPath, "test data " + clientId + " " + i++);
            Thread.sleep(10000);
        }
    }
}
