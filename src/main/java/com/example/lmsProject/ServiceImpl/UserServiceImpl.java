package com.example.lmsProject.ServiceImpl;
import com.example.lmsProject.Repository.UserRepository;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.EmailService;
import com.example.lmsProject.service.UserService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository repo, EmailService emailService) {
        this.userRepository = repo;
        this.emailService = emailService;
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
    public User createUser(User user) throws MessagingException {
        user.setCreatedAt(LocalDateTime.now());
        User createdUser = userRepository.save(user);
        emailService.sendCreateUserNotification(
                createdUser.getEmail(), createdUser.getEmail(), createdUser.getPasswordHash(), createdUser.getFullName()
        );
        return createdUser;
    }

    @Override
    public User updateUser(Integer id, User user) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPasswordHash(user.getPasswordHash());
            existingUser.setRole(user.getRole());
            return userRepository.save(existingUser);
        }).orElse(null);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDto convertUserToUserDto(User user) {
        return new UserDto(user.getUserId(), user.getFullName(),user.getEmail(), user.getRole().getRoleName());
    }
}
