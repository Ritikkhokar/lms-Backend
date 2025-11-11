package com.example.lmsProject.service;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.User;
import jakarta.mail.MessagingException;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer id);
    User createUser(User user) throws MessagingException;
    User updateUser(Integer id, User user);
    void deleteUser(Integer id);
    User findByEmail(String email);
    UserDto convertUserToUserDto(User user);
}
