package netty.customized;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CustomV1Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        int dataLength = internalBuffer().readableBytes();
        if (dataLength <= 0) {
            return;
        }

        byte[] data = new byte[dataLength];
        internalBuffer().readBytes(data);

        Object obj = HessianSerializer.deserialize(data);
        out.add(obj);
    }
}
