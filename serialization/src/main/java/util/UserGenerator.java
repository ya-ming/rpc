package util;

import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGenerator {
    public static User genUser() {
        User u1 = new User();
        u1.setName("user 1");
        u1.setEmail("user1@example.com");

        User u2 = new User();
        u2.setName("user 1");
        u2.setEmail("user1@example.com");

        List<User> userList = new ArrayList<>();
        Map<String, User> userMap = new HashMap<>();
        userList.add(u2);
        userMap.put("b", u2);

        u1.setUserList(userList);
        u1.setUserMap(userMap);

        return u1;
    }
}
