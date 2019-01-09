package netty.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class ProtobufClientHandler extends ChannelInboundHandlerAdapter {
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserInfo.User user = UserInfo.User.newBuilder()
                .setName("user1")
                .setUserId(10000)
                .setEmail("user1@example.com")
                .setMobile("10086")
                .setRemark("remark info")
                .build();

        ctx.writeAndFlush(user);
    }

    // close the connection on exception
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
