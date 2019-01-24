package yaming.rpc.framework.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import yaming.rpc.framework.helper.PropertyConfigHelper;
import yaming.rpc.framework.model.RpcRequest;
import yaming.rpc.framework.serialization.NettyDecoderHandler;
import yaming.rpc.framework.serialization.NettyEncoderHandler;
import yaming.rpc.framework.serialization.common.SerializerType;

public class NettyServer {
    private static NettyServer nettyServer = new NettyServer();

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SerializerType serializerType = PropertyConfigHelper.getSerializerType();

    /**
     * Start Netty Service
     * @param port
     */
    public void start(final int port) {
        synchronized (NettyServer.class) {
            if (bossGroup != null || workerGroup != null) {
                return;
            }

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // register decoder NettyDecoderHandler
                            ch.pipeline().addLast(new NettyDecoderHandler(RpcRequest.class, serializerType));
                            // register encoder NettyEncoderHandler
                            ch.pipeline().addLast(new NettyEncoderHandler(serializerType));
                            // register event handler NettyServerInvokeHandler
                            ch.pipeline().addLast(new NettyServerInvokeHandler());
                        }
                    });
            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Stop Netty Service
     */
    public void stop() {
        if (null == channel) {
            throw new RuntimeException("Netty Server Stopped");
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }


    private NettyServer() {
    }


    public static NettyServer singleton() {
        return nettyServer;
    }
}
