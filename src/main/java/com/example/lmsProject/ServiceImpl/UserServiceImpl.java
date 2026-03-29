package com.example.lmsProject.ServiceImpl;
import com.example.lmsProject.Repository.UserRepository;
import com.example.lmsProject.dto.UserDto;
import com.example.lmsProject.dto.UserEvent;
import com.example.lmsProject.entity.User;
import com.example.lmsProject.service.EmailService;
import com.example.lmsProject.service.UserEventProducer;
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
    private final UserEventProducer userEventProducer;

    public UserServiceImpl(UserRepository repo, EmailService emailService, UserEventProducer userEventProducer) {
        this.userRepository = repo;
        this.emailService = emailService;
        this.userEventProducer = userEventProducer;
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
        UserEvent event = new UserEvent(
                "USER_CREATED",
                createdUser.getUserId(),
                createdUser.getFullName(),
                createdUser.getEmail(),
                createdUser.getRole().getRoleName(),
                LocalDateTime.now()
        );
        userEventProducer.publishUserEvent(event);

        logger.info("Created user {}, published USER_CREATED event", createdUser.getUserId());
        return createdUser;
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userByEmail", "userById"}, allEntries = true)
    public User updateUser(Integer id, User user) throws MessagingException{
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
        emailService.sendUpdateUserNotification(
                existingUser.getEmail(), existingUser.getEmail(), existingUser.getPasswordHash(), existingUser.getFullName()
        );
        logger.info("Updated user with id {}, evicted user caches", id);

        UserEvent event = new UserEvent(
                "USER_UPDATED",
                updated.getUserId(),
                updated.getFullName(),
                updated.getEmail(),
                updated.getRole().getRoleName(),
                LocalDateTime.now()
        );
        userEventProducer.publishUserEvent(event);
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

        UserEvent event = new UserEvent(
                "USER_DELETED",
                id,
                null,
                null,
                null,
                LocalDateTime.now()
        );
        userEventProducer.publishUserEvent(event);

        logger.info("Deleted user {}, published USER_DELETED event", id);
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
