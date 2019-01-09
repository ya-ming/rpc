package netty.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class ProtobufServerHandler extends SimpleChannelInboundHandler {
    private static final AtomicInteger counter = new AtomicInteger(0);

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // receive the data from the client
        UserInfo.User req = (UserInfo.User) msg;
        System.out.println("received from client: " + req.toString());
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
