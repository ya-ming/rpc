package yaming.rpc.framework.serialization.serializer.impl;

import org.apache.commons.lang3.reflect.MethodUtils;
import yaming.rpc.framework.serialization.serializer.ISerializer;

public class ProtocolBufferSerializer implements ISerializer {


    public <T> byte[] serialize(T obj) {
        try {
            return (byte[]) MethodUtils.invokeMethod(obj, "toByteArray");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            Object o = MethodUtils.invokeStaticMethod(cls, "getDefaultInstance");
            return (T) MethodUtils.invokeMethod(o, "parseFrom", new Object[]{data});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
