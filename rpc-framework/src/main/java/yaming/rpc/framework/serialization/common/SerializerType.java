package yaming.rpc.framework.serialization.common;

import org.apache.commons.lang.StringUtils;

public enum SerializerType {
    DefaultJavaSerializer("DefaultJavaSerializer"),
    HessianSerializer("HessianSerializer"),
    JSONSerializer("JSONSerializer"),
    ProtoStuffSerializer("ProtoStuffSerializer"),
    XmlSerializer("XmlSerializer"),
    MarshallingSerializer("MarshallingSerializer"),

    AvroSerializer("AvroSerializer"),
    ProtocolBufferSerializer("ProtocolBufferSerializer"),
    ThriftSerializer("ThriftSerializer");

    private String serializeType;

    private SerializerType(String serializeType) {
        this.serializeType = serializeType;
    }


    public static SerializerType queryByType(String serializeType) {
        if (StringUtils.isBlank(serializeType)) {
            return null;
        }

        for (SerializerType serialize : SerializerType.values()) {
            if (StringUtils.equals(serializeType, serialize.getSerializeType())) {
                return serialize;
            }
        }
        return null;
    }

    public String getSerializeType() {
        return serializeType;
    }
}
