package com.example.lmsProject.ServiceImpl;
import com.example.lmsProject.Repository.UserRepository;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.EmailService;
import com.example.lmsProject.service.UserService;
import jakarta.mail.MessagingException;
import com.example.lmsProject.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = "users")
    public List<User> getAllUsers() {
        logger.info("Get all users - fetching from database -> cache miss");
        return userRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "userById", key = "#id")
    public User getUserById(Integer id) {
        logger.info("Fetching user by id {} from database (cache miss)", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userByEmail", "userById"}, allEntries = true)
    public User createUser(User user) throws MessagingException {
        user.setCreatedAt(LocalDateTime.now());
        User createdUser = userRepository.save(user);
        emailService.sendCreateUserNotification(
                createdUser.getEmail(), createdUser.getEmail(), createdUser.getPasswordHash(), createdUser.getFullName()
        );
        logger.info("Created new user with id {}, evicted user caches", createdUser.getUserId());
        return createdUser;
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userByEmail", "userById"}, allEntries = true)
    public User updateUser(Integer id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getFullName() != null) {
            existingUser.setFullName(user.getFullName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPasswordHash() != null) {
            existingUser.setPasswordHash(user.getPasswordHash());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }

        User updated = userRepository.save(existingUser);
        logger.info("Updated user with id {}, evicted user caches", id);
        return updated;
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userByEmail", "userById"}, allEntries = true)
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        logger.info("Delete user with id {}, evicted user caches", id);
    }

    @Override
    @Cacheable(cacheNames = "userByEmail", key = "#email")
    public User findByEmail(String email) {
        logger.info("Get user by email - fetching from database -> cache miss");
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDto convertUserToUserDto(User user) {
        return new UserDto(user.getUserId(), user.getFullName(),user.getEmail(), user.getRole().getRoleName());
    }
}
