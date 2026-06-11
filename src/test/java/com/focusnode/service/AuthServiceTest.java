package com.focusnode.service;

import com.focusnode.model.User;
import com.focusnode.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    public void testLogin_Success() {
        String username = "testuser";
        String password = "hashpassword";
        User mockUser = new User(1, username, "test@example.com", java.time.LocalDateTime.now());

        when(userRepository.authenticate(username, password)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(mockUser);

        boolean result = authService.login(username, password);

        assertTrue(result);
        assertTrue(authService.isLoggedIn());
        assertEquals(mockUser, authService.getCurrentUser());
        
        verify(userRepository, times(1)).authenticate(username, password);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    public void testLogin_Failure() {
        String username = "testuser";
        String password = "wrongpassword";

        when(userRepository.authenticate(username, password)).thenReturn(false);

        boolean result = authService.login(username, password);

        assertFalse(result);
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());

        verify(userRepository, times(1)).authenticate(username, password);
        verify(userRepository, never()).findByUsername(username);
    }

    @Test
    public void testLogout() {
        // Setup a logged in state
        String username = "testuser";
        String password = "hashpassword";
        User mockUser = new User(1, username, "test@example.com", java.time.LocalDateTime.now());
        
        when(userRepository.authenticate(username, password)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(mockUser);
        
        authService.login(username, password);
        assertTrue(authService.isLoggedIn());

        // Test logout
        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }

    @Test
    public void testRegister() {
        String username = "newuser";
        String email = "newuser@example.com";
        String password = "hashpassword";

        when(userRepository.createUser(username, email, password)).thenReturn(true);

        boolean result = authService.register(username, email, password);

        assertTrue(result);
        verify(userRepository, times(1)).createUser(username, email, password);
    }
}
