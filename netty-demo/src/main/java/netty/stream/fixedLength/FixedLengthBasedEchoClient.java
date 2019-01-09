package netty.stream.fixedLength;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class FixedLengthBasedEchoClient {
    public static void main(String[] args) {
        int port = 8081;
        new FixedLengthBasedEchoClient().connect(port, "127.0.0.1");
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
                            // set FixedLengthFrameDecoder
                            nioSocketChannel.pipeline().addLast(new FixedLengthFrameDecoder(13));
                            // set StringDecoder
                            nioSocketChannel.pipeline().addLast(new StringDecoder());
                            // configure the I/O event handler of the client
                            nioSocketChannel.pipeline().addLast(new FixedLengthBasedEchoClientHandler());
                        }
                    });
            // initiate async connection
            ChannelFuture future = bootstrap.connect(host, port).sync();

            for (int i = 0; i < 1000; i++) {
                // prepare the data to send
                String content = "Hello, Netty!";
                byte[] req = content.getBytes();
                ByteBuf messageBuffer = Unpooled.buffer(req.length);
                messageBuffer.writeBytes(req);

                // send data to the server
                ChannelFuture channelFuture = future.channel().writeAndFlush(messageBuffer);
                channelFuture.syncUninterruptibly();
            }

            // wait for closing the connection
            future.channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
