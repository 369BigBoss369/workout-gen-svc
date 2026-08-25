package com.workoutgensvc.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User findOrCreateUser(UUID parsedUserId, String rawUserId) {
        User user = null;
        int retries = 3;

        for (int i = 0; i < retries && user == null; i++) {
            user = userRepository.findById(parsedUserId).orElse(null);

            if (user == null) {
                log.info("User {} not found, auto-creating user (attempt {})", rawUserId, i + 1);

                String username = "user-" + rawUserId.substring(0, 8);
                String email = "user-" + rawUserId.substring(0, 8) + "@auto-generated.com";

                if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
                    user = userRepository.findByUsername(username).orElse(null);
                    if (user == null) {
                        user = userRepository.findByEmail(email).orElse(null);
                    }
                    if (user != null) {
                        log.debug("Found existing user {} with same username/email", user.getId());
                        continue;
                    }
                }

                // These users exist only for internal bookkeeping (tracking generated
                // content per Momentum user ID) - they never log in, so this is never
                // a real credential, just a non-null placeholder to satisfy the column.
                User newUser = User.builder()
                        .id(parsedUserId)
                        .username(username)
                        .email(email)
                        .password("NOT_A_REAL_PASSWORD-INTERNAL_USER_ONLY")
                        .build();

                try {
                    user = userRepository.saveAndFlush(newUser);
                    log.debug("Successfully created user {}", rawUserId);
                } catch (Exception e) {
                    log.debug("Failed to create user {} on attempt {}: {}", rawUserId, i + 1, e.getMessage());
                    if (i == retries - 1) {
                        throw new RuntimeException("Failed to create or find user after " + retries + " attempts: " + rawUserId, e);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return user;
    }

    public void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
}


