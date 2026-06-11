package com.focusnode.service;

import com.focusnode.model.User;
import com.focusnode.repository.UserRepository;

public class AuthService {
    
    private final UserRepository userRepository;
    private User currentUser;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    // For testing
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String passwordHash) {
        if (userRepository.authenticate(username, passwordHash)) {
            this.currentUser = userRepository.findByUsername(username);
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean register(String username, String email, String passwordHash) {
        return userRepository.createUser(username, email, passwordHash);
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
