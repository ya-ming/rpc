package serializer.impl;

import model.User;
import serializer.ISerializer;
import util.UserGenerator;

import java.io.*;

public class DefaultJavaSerializer implements ISerializer {
    @SuppressWarnings("unchecked")
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        byte[] userBytes = new DefaultJavaSerializer().serialize(UserGenerator.genUser());
        User user = new DefaultJavaSerializer().deserialize(userBytes, User.class);
        System.out.println(user);
//        System.out.println(user.getEmail() + " : " + user.getName() + " : " + new String(new JSONSerializer().serialize(u1.getUserList())) + " : " + new String(new JSONSerializer().serialize(u1.getUserMap())));
    }
}
