package yaming.rpc.framework.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import yaming.rpc.framework.serialization.common.SerializerType;
import yaming.rpc.framework.serialization.engine.SerializerEngine;

import java.util.List;

public class NettyDecoderHandler extends ByteToMessageDecoder {

    // object to be decoded
    private Class<?> genericClass;
    // type of the serializer
    private SerializerType serializerType;

    public NettyDecoderHandler(Class<?> genericClass, SerializerType serializerType) {
        this.genericClass = genericClass;
        this.serializerType = serializerType;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("NettyDecoderHandler::decode() " + in);
        // get the length in the header of the message
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        // waiting for the readable length equals to the length of the message expected
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // read the whole message
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // deserialize the byte array
        Object obj = SerializerEngine.deserialize(data, genericClass, serializerType.getSerializeType());
        out.add(obj);
    }

}
