package yaming.rpc.framework.serialization.serializer;

public interface ISerializer {

    /**
     * serialize
     *
     * @param obj
     * @param <T>
     * @return
     */
    public <T> byte[] serialize(T obj);


    /**
     * deserialize
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T deserialize(byte[] data, Class<T> clazz);
}
