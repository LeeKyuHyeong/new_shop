package com.kh.shop.service;

import com.kh.shop.entity.User;
import com.kh.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean isDuplicateUserId(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    public boolean isDuplicateEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUser(String userId, String userPassword, String userName,
                             String email, String gender, LocalDate birth) {
        User user = User.builder()
                .userId(userId)
                .userPassword(userPassword)
                .userName(userName)
                .email(email)
                .gender(gender)
                .birth(birth)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> loginUser(String userId, String userPassword) {
        Optional<User> user = userRepository.findByUserId(userId);
        if (user.isPresent() && user.get().getUserPassword().equals(userPassword)) {
            return user;
        }
        return Optional.empty();
    }
}