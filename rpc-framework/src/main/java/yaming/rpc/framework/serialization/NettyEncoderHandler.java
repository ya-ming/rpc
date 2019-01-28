package yaming.rpc.framework.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import yaming.rpc.framework.serialization.common.SerializerType;
import yaming.rpc.framework.serialization.engine.SerializerEngine;

public class NettyEncoderHandler extends MessageToByteEncoder {
    // type of the serializer
    private SerializerType serializerType;

    public NettyEncoderHandler(SerializerType serializerType) {
        this.serializerType = serializerType;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        System.out.println("NettyEncoderHandler::encode() " + in);
        // serialize the object to byte array
        byte[] data = SerializerEngine.serialize(in, serializerType.getSerializeType());
        // write the length of the serialized object to the head of the message
        out.writeInt(data.length);
        // write the serialized object to the channel
        out.writeBytes(data);
    }
}
