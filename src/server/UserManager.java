package server;

import common.User;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final String AUTH_FILE = "Auth.dat";
    private ConcurrentHashMap<String, User> users;

    public UserManager() {
        users = loadUsers();
    }

    public User findUserByName(String name){
        return users.get(name);
    }

    public boolean registerUser(String id, String password) {
        if (users.containsKey(id)) {
            return false; // 이미 존재하는 사용자 이름
        }
        users.put(id, new User(id, password));
        saveUsers();
        return true;
    }

    public boolean authenticateUser(String id, String password) {
        User user = users.get(id);
        return user != null && user.getPassword().equals(password);
    }

    public void reloadUsers(){
        loadUsers();
        System.out.println("Users reloaded from file.");
    }
    private ConcurrentHashMap<String, User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(AUTH_FILE))) {
            return (ConcurrentHashMap<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Auth file not found or empty. Starting fresh.");
            return new ConcurrentHashMap<>();
        }
    }

    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(AUTH_FILE))) {
            oos.writeObject(users);
            System.out.println("common.User data saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, User> getAllUsers() {
        return users;
    }
}
