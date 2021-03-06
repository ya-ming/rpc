package netty.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ProtobufClient {
    public static void main(String[] args) {
        int port = 8081;
        new ProtobufClient().connect(port, "127.0.0.1");
    }

    private void connect(int port, String host) {
        // create thread group for the client
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // create bootstrap for the client
            Bootstrap bootstrap = new Bootstrap();
            // set NIO thread group
            bootstrap.group(group)
                    // set NioSocketChannel
                    .channel(NioSocketChannel.class)
                    // set TCP parameter to TCP_NODELAY
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            // using ProtobufVarint32LengthFieldPrepender and ProtobufEncoder
                            nioSocketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            nioSocketChannel.pipeline().addLast(new ProtobufEncoder());
                            // configure the I/O event handler of the client
                            nioSocketChannel.pipeline().addLast(new ProtobufClientHandler());
                        }
                    });
            // initiate async connection
            ChannelFuture future = bootstrap.connect(host, port).sync();

            // wait for closing the connection
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
