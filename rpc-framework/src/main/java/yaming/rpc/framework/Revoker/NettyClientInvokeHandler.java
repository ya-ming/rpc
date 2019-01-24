package yaming.rpc.framework.Revoker;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import yaming.rpc.framework.model.RpcResponse;

public class NettyClientInvokeHandler extends SimpleChannelInboundHandler<RpcResponse> {


    public NettyClientInvokeHandler() {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        // store the async-response into RevokerResponseHolder, let the invoker to retrieve
        RevokerResponseHolder.putResultValue(response);
    }


}
