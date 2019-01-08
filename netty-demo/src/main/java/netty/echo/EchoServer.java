package netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoServer {
    public static void main(String[] args) {
        int port = 8081;
        new EchoServer().bind(port);
    }

    private void bind(int port) {
        // create two instance of EventLoopGroup
        // EventLoopGroup is a NIO thread group to handle network events
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // create bootstrap for server
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // set NIO thread group
            serverBootstrap.group(bossGroup, workerGroup)
                // set NioServerSocketChannel, this is corresponding to ServerSocketChannel of JDK NIO
                .channel(NioServerSocketChannel.class)
                // set TCP parameter, queue depth of connection request
                .option(ChannelOption.SO_BACKLOG, 1024)
                // set I/O handler class for encoding/decoding and service logic
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new EchoServerHandler());
                    }
                });
            // bind to port and wait for sync
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // wait for the server port to be closed
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
