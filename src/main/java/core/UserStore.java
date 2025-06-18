package core;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static final Map<String, String> validUsers = new HashMap<>();

    static {
        validUsers.put("admin", "1234");
        validUsers.put("root", "root");
        validUsers.put("user", "senha");
        validUsers.put("teste", "teste");
    }

    public static boolean isValid(String username, String password) {
        return validUsers.containsKey(username) && validUsers.get(username).equals(password);
    }
}
