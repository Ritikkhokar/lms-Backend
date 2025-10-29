package com.example.lmsProject.service;
import com.example.lmsProject.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer id);
    User createUser(User user);
    User updateUser(Integer id, User user);
    void deleteUser(Integer id);
    User findByEmail(String email);
}
