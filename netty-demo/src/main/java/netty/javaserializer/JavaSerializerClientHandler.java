package netty.javaserializer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaSerializerClientHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserInfo user = UserInfo.newBuilder()
                .name("user1")
                .userId(10000)
                .email("user1@example.com")
                .mobile("10086")
                .remark("remark info")
                .build();

        ctx.writeAndFlush(user);
    }

    // close the connection on exception
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
