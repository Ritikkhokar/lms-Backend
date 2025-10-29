package com.example.lmsProject.ServiceImpl;
import com.example.lmsProject.Repository.UserRepository;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPasswordHash(user.getPasswordHash());
            existingUser.setRole(user.getRole());
            existingUser.setCreatedAt(user.getCreatedAt());
            return userRepository.save(existingUser);
        }).orElse(null);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
