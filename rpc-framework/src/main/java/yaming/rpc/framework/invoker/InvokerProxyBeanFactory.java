package yaming.rpc.framework.invoker;

import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.model.RpcRequest;
import yaming.rpc.framework.model.RpcResponse;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Invoker;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InvokerProxyBeanFactory implements InvocationHandler {

    private ExecutorService fixedThreadPool = null;

    // service interface
    private Class<?> targetInterface;
    // timeout timer
    private int consumeTimeout;
    // number of worker threads
    private static int threadWorkerNumber = 10;
    // strategy of loading balance
    private String clusterStrategy;


    public InvokerProxyBeanFactory(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) {
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // get the interface name
        String serviceKey = targetInterface.getName();
        // connect to ZooKeeper and retrieve the list of service provider based on the service key
        IRegisterCenter4Invoker registerCenter4Consumer = RegisterCenter.singleton();
        List<ProviderService> providerServices = registerCenter4Consumer.getServiceMetaDataMap4Consumer().get(serviceKey);
        // base on the loading balance strategy, select one service provider
//        ClusterStrategy clusterStrategyService = ClusterEngine.queryClusterStrategy(clusterStrategy);
//        ProviderService providerService = clusterStrategyService.select(providerServices);
        ProviderService providerService = providerServices.get(0);
        // make a copy of the service provider
        ProviderService newProvider = providerService.copy();
        // set the 'method' and 'interface' to be used for this invoke
        newProvider.setServiceMethod(method);
        newProvider.setServiceItf(targetInterface);

        // create RpcRequest, it contains all the info needed for the RPC call
        final RpcRequest request = new RpcRequest();
        // set unique key for this invoke
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread().getId());
        // set service provider
        request.setProviderService(newProvider);
        // set timeout timer
        request.setInvokeTimeout(consumeTimeout);
        // set method name
        request.setInvokedMethodName(method.getName());
        // set args for the invoke
        request.setArgs(args);

        try {
            // create thread pool for initiate the rpc
            if (fixedThreadPool == null) {
                synchronized (InvokerProxyBeanFactory.class) {
                    if (null == fixedThreadPool) {
                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                    }
                }
            }
            // create InetSocketAddress for the service provider
            String serverIp = request.getProviderService().getServerIp();
            int serverPort = request.getProviderService().getServerPort();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
            // submit the thread to fixedThreadPoll, invoke the rpc
            Future<RpcResponse> responseFuture = fixedThreadPool.submit(InvokerServiceCallable.of(inetSocketAddress, request));
            // retrieve the response from the rpc
            RpcResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if (response != null) {
                return response.getResult();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public Object getProxy() {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{targetInterface}, this);
    }


    private static volatile InvokerProxyBeanFactory singleton;

    public static InvokerProxyBeanFactory singleton(Class<?> targetInterface, int consumeTimeout, String clusterStrategy) throws Exception {
        if (null == singleton) {
            synchronized (InvokerProxyBeanFactory.class) {
                if (null == singleton) {
                    singleton = new InvokerProxyBeanFactory(targetInterface, consumeTimeout, clusterStrategy);
                }
            }
        }
        return singleton;
    }
}