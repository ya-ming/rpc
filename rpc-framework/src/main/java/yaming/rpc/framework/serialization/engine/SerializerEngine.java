package yaming.rpc.framework.serialization.engine;


import avro.shaded.com.google.common.collect.Maps;
import yaming.rpc.framework.serialization.common.SerializerType;
import yaming.rpc.framework.serialization.serializer.ISerializer;
import yaming.rpc.framework.serialization.serializer.impl.*;

import java.util.Map;

/**
 * @author liyebing created on 17/1/23.
 * @version $Id$
 */
public class SerializerEngine {

    public static final Map<SerializerType, ISerializer> serializerMap = Maps.newConcurrentMap();

    static {
        serializerMap.put(SerializerType.DefaultJavaSerializer, new DefaultJavaSerializer());
        serializerMap.put(SerializerType.HessianSerializer, new HessianSerializer());
        serializerMap.put(SerializerType.JSONSerializer, new JSONSerializer());
        serializerMap.put(SerializerType.XmlSerializer, new XmlSerializer());
        serializerMap.put(SerializerType.ProtoStuffSerializer, new ProtoStuffSerializer());
        serializerMap.put(SerializerType.MarshallingSerializer, new MarshallingSerializer());

        //以下三类不能使用普通的java bean
        serializerMap.put(SerializerType.AvroSerializer, new AvroSerializer());
        serializerMap.put(SerializerType.ThriftSerializer, new ThriftSerializer());
        serializerMap.put(SerializerType.ProtocolBufferSerializer, new ProtocolBufferSerializer());
    }


    public static <T> byte[] serialize(T obj, String serializeType) {
        SerializerType serializerType = SerializerType.queryByType(serializeType);
        if (serializerType == null) {
            throw new RuntimeException("serializerType is null");
        }

        ISerializer serializer = serializerMap.get(serializerType);
        if (serializer == null) {
            throw new RuntimeException("serializer error");
        }

        try {
            return serializer.serialize(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T deserialize(byte[] data, Class<T> clazz, String serializeType) {

        SerializerType serializerType = SerializerType.queryByType(serializeType);
        if (serializerType == null) {
            throw new RuntimeException("serializerType is null");
        }
        ISerializer serializer = serializerMap.get(serializerType);
        if (serializer == null) {
            throw new RuntimeException("serializer error");
        }

        try {
            return serializer.deserialize(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
