package com.example.chatroom.service;

import com.example.chatroom.entity.User;
import com.example.chatroom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
    }

    // 语句覆盖测试用例
    @Test
    void testValidateUser_UserExistsAndPasswordCorrect() {
        // 语句覆盖：测试用户存在且密码正确的路径
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

        boolean result = userService.validateUser("testuser", "correctPassword");

        assertTrue(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("correctPassword", "encodedPassword");
    }

    @Test
    void testValidateUser_UserExistsButPasswordIncorrect() {
        // 语句覆盖：测试用户存在但密码错误的路径
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        boolean result = userService.validateUser("testuser", "wrongPassword");

        assertFalse(result);
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    void testValidateUser_UserNotExists() {
        // 语句覆盖：测试用户不存在的路径
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        boolean result = userService.validateUser("nonexistent", "anyPassword");

        assertFalse(result);
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // 分支覆盖测试用例
    @Test
    void testRegisterUser_NewUserSuccess() {
        // 分支覆盖：测试注册新用户成功分支
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainPassword");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2);
            return user;
        });

        User result = userService.registerUser(newUser);

        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_UsernameAlreadyExists() {
        // 分支覆盖：测试用户名已存在分支
        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(existingUser);
        });

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    // 路径覆盖测试用例
    @Test
    void testUpdateUserOnlineStatus_UserExists() {
        // 路径覆盖：测试更新在线状态-用户存在路径
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUserOnlineStatus("testuser", true);

        assertTrue(testUser.isOnline());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserOnlineStatus_UserNotExists() {
        // 路径覆盖：测试更新在线状态-用户不存在路径
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        userService.updateUserOnlineStatus("nonexistent", true);

        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any());
    }
}
