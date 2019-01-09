package netty.customized;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class CustomizedClient {
    public static void main(String[] args) {
        int port = 8081;
        new CustomizedClient().connect(port, "127.0.0.1");
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
                            nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65503, 0, 2, 0, 2));
                            nioSocketChannel.pipeline().addLast(new CustomV1Decoder());
                            nioSocketChannel.pipeline().addLast(new LengthFieldPrepender(2));
                            nioSocketChannel.pipeline().addLast(new CustomV1Encoder());
                            nioSocketChannel.pipeline().addLast(new CustomizedClientHandler());
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
