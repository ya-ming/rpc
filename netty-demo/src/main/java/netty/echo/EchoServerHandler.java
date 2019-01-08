package netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

public class EchoServerHandler extends SimpleChannelInboundHandler {
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // receive the data from the client
        ByteBuf buf = (ByteBuf) msg;
        // create a byte array for the data
        byte[] req = new byte[buf.readableBytes()];
        // read the data
        buf.readBytes(req);

        // convert byte array to string
        String body = new String(req, "UTF-8");
        System.out.println("received data from client: " + body);

        // send the received data back to the client
        ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
        ctx.write(resp);
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
