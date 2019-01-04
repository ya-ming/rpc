package serializer.impl;

import model.User;
import serializer.ISerializer;
import util.UserGenerator;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class XMLSerializer2 implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder xe = new XMLEncoder(out, "utf-8", true, 0);
        xe.writeObject(obj);
        xe.close();
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        XMLDecoder xd = new XMLDecoder(new ByteArrayInputStream(data));
        Object obj = xd.readObject();
        xd.close();
        return (T) obj;
    }

    public static void main(String[] args) {
        byte[] userBytes = new XMLSerializer2().serialize(UserGenerator.genUser());
        User user = new XMLSerializer2().deserialize(userBytes, User.class);
        System.out.println(user);
    }
}
