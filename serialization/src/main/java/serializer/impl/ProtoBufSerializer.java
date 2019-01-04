package serializer.impl;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import model.AddressBookProtos;
import org.apache.commons.lang3.reflect.MethodUtils;
import serializer.ISerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ProtoBufSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        try {
            if (!(obj instanceof GeneratedMessageV3)) {
                throw new UnsupportedOperationException("not supported obj type");
            }

            return (byte[]) MethodUtils.invokeMethod(obj, "toByteArray");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            if (!GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("not supported obj type");
            }
            Object o = MethodUtils.invokeExactStaticMethod(clazz, "getDefaultInstance");
            return (T) MethodUtils.invokeMethod(o, "parseFrom", new Object[]{data});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder()
                .setName("user 1")
                .setEmail("user1@example.com")
                .setId(9999)
                .addPhone(
                        AddressBookProtos.Person.PhoneNumber.newBuilder()
                        .setType(AddressBookProtos.Person.PhoneType.MOBILE)
                        .setNumber("10086").build()
                )
                .build();

        // Serialize
        System.out.println(person.toString());
        System.out.println(Arrays.toString(person.toByteArray()));

        // Deserialize method 1
        try {
            AddressBookProtos.Person newPerson = AddressBookProtos.Person.parseFrom(person.toByteString());
            System.out.println(newPerson);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // Deserialize method 2
        try {
            AddressBookProtos.Person newPerson = AddressBookProtos.Person.parseFrom(person.toByteArray());
            System.out.println(newPerson);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        // Add 2 entries to the address book
        AddressBookProtos.AddressBook book = AddressBookProtos.AddressBook.newBuilder()
                .addPerson(person)
                .addPerson(AddressBookProtos.Person.newBuilder().setName("user 2").setEmail("user2@example.com").setId(8888).build()).build();

        System.out.println("book:\n" + book);

        System.out.println("------------------------------------------------");
        byte[] data = new ProtoBufSerializer().serialize(person);
        AddressBookProtos.Person personCopy = new ProtoBufSerializer().deserialize(data, AddressBookProtos.Person.class);
        System.out.println(personCopy);
    }
}
