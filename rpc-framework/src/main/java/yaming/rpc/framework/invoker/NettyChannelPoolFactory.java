package yaming.rpc.framework.invoker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yaming.rpc.framework.helper.PropertyConfigHelper;
import yaming.rpc.framework.model.ProviderService;
import yaming.rpc.framework.model.RpcResponse;
import yaming.rpc.framework.serialization.NettyDecoderHandler;
import yaming.rpc.framework.serialization.NettyEncoderHandler;
import yaming.rpc.framework.serialization.common.SerializerType;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class NettyChannelPoolFactory {
    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPoolFactory.class);

    private static final NettyChannelPoolFactory channelPoolFactory = new NettyChannelPoolFactory();

    // Key: address of the service provider
    // value: blocking array of Netty Channel
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> channelPoolMap = Maps.newConcurrentMap();
    // init the length of blocking array
    private static final int channelConnectSize = PropertyConfigHelper.getChannelConnectSize();
    // init the type of serializer
    private static final SerializerType serializerType = PropertyConfigHelper.getSerializerType();
    // list of service provider
    private List<ProviderService> serviceMetaDataList = Lists.newArrayList();


    private NettyChannelPoolFactory() {
    }


    /**
     * init the map of blocking queue for Netty channels
     *
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap) {
        // save info of service provider into serviceMetaDataList
        Collection<List<ProviderService>> collectionServiceMetaDataList = providerMap.values();
        for (List<ProviderService> serviceMetaDataModels : collectionServiceMetaDataList) {
            if (CollectionUtils.isEmpty(serviceMetaDataModels)) {
                continue;
            }
            serviceMetaDataList.addAll(serviceMetaDataModels);
        }

        // retrieve addresses of service providers
        Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();
        for (ProviderService serviceMetaData : serviceMetaDataList) {
            String serviceIp = serviceMetaData.getServerIp();
            int servicePort = serviceMetaData.getServerPort();

            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp, servicePort);
            socketAddressSet.add(socketAddress);
        }

        // create Netty Channel for each service provider
        // create blocking queue based on the address of the service provider
        // store the blocking into channelPoolMap
        // Key: address
        // Value: blocking queue of that channel
        for (InetSocketAddress socketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < channelConnectSize) {
                    Channel channel = null;
                    while (channel == null) {
                        // register new Netty channel if not exist
                        channel = registerChannel(socketAddress);
                    }
                    // counter, limit the number of NettyChannels can be stored in the channel pool map
                    realChannelConnectSize++;

                    // store the newly registered Netty Channel into channelArrayBlockingQueue
                    // then, store the channelArrayBlockingQueue into channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelPoolMap.get(socketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<Channel>(channelConnectSize);
                        channelPoolMap.put(socketAddress, channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * return blocking queue of a netty channel based on the address of the service provider
     *
     * @param socketAddress
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return channelPoolMap.get(socketAddress);
    }


    /**
     * release the channel back to arrayBlockingQueue after end of usage
     *
     * @param arrayBlockingQueue
     * @param channel
     * @param inetSocketAddress
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress inetSocketAddress) {
        if (arrayBlockingQueue == null) {
            return;
        }

        // before release, make sure the channel is still usable, otherwise, register a new one
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (newChannel == null) {
                logger.debug("---------register new Channel-------------");
                newChannel = registerChannel(inetSocketAddress);
            }
            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }


    /**
     * register channel for the socketAddress of a service provider
     *
     * @param socketAddress
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // register Netty encoder
                            ch.pipeline().addLast(new NettyEncoderHandler(serializerType));
                            // register Netty decoder
                            ch.pipeline().addLast(new NettyDecoderHandler(RpcResponse.class, serializerType));
                            // register the handler of client
                            ch.pipeline().addLast(new NettyClientInvokeHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch connectedLatch = new CountDownLatch(1);

            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            // watch the result of creating the new channel
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    // if channel created successfully, set isSuccessHolder to true
                    if (future.isSuccess()) {
                        System.out.println("NettyChannelPoolFactory::registerChannel" + "success");
                        isSuccessHolder.add(Boolean.TRUE);
                    } else {
                        // otherwise, set isSuccessHolder = false
                        System.out.println("NettyChannelPoolFactory::registerChannel" + "fail");
                        future.cause().printStackTrace();
                        isSuccessHolder.add(Boolean.FALSE);
                    }
                    connectedLatch.countDown();
                }
            });

            connectedLatch.await();
            // return the new channel if channel created successfully
            if (isSuccessHolder.get(0)) {
                return newChannel;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public static NettyChannelPoolFactory channelPoolFactoryInstance() {
        return channelPoolFactory;
    }
}
