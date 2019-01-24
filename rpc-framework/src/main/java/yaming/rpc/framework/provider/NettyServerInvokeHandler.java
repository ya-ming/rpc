package yaming.rpc.framework.provider;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.model.RpcRequest;
import yaming.rpc.framework.model.RpcResponse;
import yaming.rpc.framework.zookeeper.IRegisterCenter4Provider;
import yaming.rpc.framework.zookeeper.RegisterCenter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class NettyServerInvokeHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerInvokeHandler.class);

    private static final Map<String, Semaphore> serviceKeySemaphoreMap = Maps.newConcurrentMap();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        if (ctx.channel().isWritable()) {
            // retrieve the info of service provider from the Request
            ProviderService metaDataModel = rpcRequest.getProviderService();
            long consumeTimeOut = rpcRequest.getInvokeTimeout();
            final String methodName = rpcRequest.getInvokedMethodName();

            // retrieve the name of the service interface
            String serviceKey = metaDataModel.getServiceItf().getName();
            int workerThread = metaDataModel.getWorkerThreads();
            Semaphore semaphore = serviceKeySemaphoreMap.get(serviceKey);
            if (semaphore == null) {
                synchronized (serviceKeySemaphoreMap) {
                    semaphore = serviceKeySemaphoreMap.get(serviceKey);
                    if (semaphore == null) {
                        semaphore = new Semaphore(workerThread);
                        serviceKeySemaphoreMap.put(serviceKey, semaphore);
                    }
                }
            }

            //
            IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
            List<ProviderService> localProviderCaches = registerCenter4Provider.getProviderServiceMap().get(serviceKey);

            Object result = null;
            boolean acquire = false;

            try {
                ProviderService localProviderCache = Collections2.filter(localProviderCaches, new Predicate<ProviderService>() {
                    @Override
                    public boolean apply(ProviderService input) {
                        return StringUtils.equals(input.getServiceMethod().getName(), methodName);
                    }
                }).iterator().next();
                Object serviceObject = localProviderCache.getServiceObject();

                // invoke the service via reflect
                Method method = localProviderCache.getServiceMethod();
                acquire = semaphore.tryAcquire(consumeTimeOut, TimeUnit.MILLISECONDS);
                if (acquire) {
                    result = method.invoke(serviceObject, rpcRequest.getArgs());
                }
            } catch (Exception e) {
                System.out.println(JSON.toJSONString(localProviderCaches) + " " + methodName + " " + e.getMessage());
                result = e;
            } finally {
                if (acquire) {
                    semaphore.release();
                }
            }

            // construct the response based on the result
            RpcResponse response = new RpcResponse();
            response.setInvokeTimeout(consumeTimeOut);
            response.setUniqueKey(rpcRequest.getUniqueKey());
            response.setResult(result);

            ctx.writeAndFlush(response);
        } else {
            logger.error("----channel closed!----");
        }
    }
}
