package netty.stream.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class DelimiterBasedEchoClientHandler extends SimpleChannelInboundHandler {
    private static final AtomicInteger counter = new AtomicInteger(0);

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // receive the data from the server
        String content = (String) msg;
        System.out.println("received data from client: " + content + " counter: " + counter.addAndGet(1));
    }

    // close the connection on exception
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    // write data in the sending buffer to the SocketChannel
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
