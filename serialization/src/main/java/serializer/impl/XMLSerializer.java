package serializer.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import model.User;
import serializer.ISerializer;
import util.UserGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLSerializer implements ISerializer {
    private static final XStream xStream = new XStream(new DomDriver());

    @Override
    public <T> byte[] serialize(T obj) {
        return xStream.toXML(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String xml = new String(data);
        return (T) xStream.fromXML(xml);
    }

    public static void main(String[] args) {
        byte[] userBytes = new XMLSerializer().serialize(UserGenerator.genUser());
        User user = new XMLSerializer().deserialize(userBytes, User.class);
        System.out.println(user);
    }

}
