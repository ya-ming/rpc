package rpc.common;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService{
    private static final Map<String, User> userMap = new HashMap<>();

    static {
        userMap.put("user1", new User("user1", "user1@example.com"));
        userMap.put("user2", new User("user2", "user2@example.com"));
    }
    public User findByName(String userName) {
        return userMap.get(userName);
    }
}
