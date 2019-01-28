package yaming.rpc.framework.Revoker;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaming.rpc.framework.model.RpcRequest;
import yaming.rpc.framework.model.RpcResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * the thread to initiate request to the netty server
 */
public class RevokerServiceCallable implements Callable<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RevokerServiceCallable.class);

    private Channel channel;
    private InetSocketAddress inetSocketAddress;
    private RpcRequest request;

    public static RevokerServiceCallable of(InetSocketAddress inetSocketAddress, RpcRequest request) {
        return new RevokerServiceCallable(inetSocketAddress, request);
    }


    public RevokerServiceCallable(InetSocketAddress inetSocketAddress, RpcRequest request) {
        this.inetSocketAddress = inetSocketAddress;
        this.request = request;
    }

    @Override
    public RpcResponse call() throws Exception {
        System.out.println("RevokerServiceCallable::call() request = " + request);
        // init the response holder, save the RpcResponseWrapper to responseMap based on unique key of this invoke
        RevokerResponseHolder.initResponseData(request.getUniqueKey());
        // acquire the netty channel blocking queue of the target address
        ArrayBlockingQueue<Channel> blockingQueue = NettyChannelPoolFactory.channelPoolFactoryInstance().acquire(inetSocketAddress);
        try {
            if (channel == null) {
                // get a netty channel from the blocking queue
                System.out.println("RevokerServiceCallable::call() get a netty channel from the blocking queue");
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            }

            // if the channel is not usable, try again
            while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                logger.warn("----------retry get new Channel------------");
                channel = blockingQueue.poll(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
                if (channel == null) {
                    // no channel available in the queue, register a new netty channel
                    channel = NettyChannelPoolFactory.channelPoolFactoryInstance().registerChannel(inetSocketAddress);
                }
            }

            // write and flush the request into netty channel, invoke the async call
            System.out.println("RevokerServiceCallable::call() write and flush the request into netty channel, invoke the async call");
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();

            // retrieve the response from the response holder and set invoke timeout timer
            System.out.println("RevokerServiceCallable::call() retrieve the response from the response holder and set invoke timeout timer");
            long invokeTimeout = request.getInvokeTimeout();
            return RevokerResponseHolder.getValue(request.getUniqueKey(), invokeTimeout);
        } catch (Exception e) {
            logger.error("service invoke error.", e);
        } finally {
            // release the netty channel then it can be reused for other RPC
            NettyChannelPoolFactory.channelPoolFactoryInstance().release(blockingQueue, channel, inetSocketAddress);
        }
        return null;
    }
}
