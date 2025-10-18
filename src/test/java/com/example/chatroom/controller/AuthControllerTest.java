package com.example.chatroom.controller;

import com.example.chatroom.entity.User;
import com.example.chatroom.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthController authController;

    private Map<String, String> credentials;

    @BeforeEach
    void setUp() {
        credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "password123");
    }

    // 路径覆盖测试用例
    @Test
    void testLogin_Success() {
        // 路径覆盖：测试登录成功路径
        when(userService.validateUser("testuser", "password123")).thenReturn(true);

        ResponseEntity<?> response = authController.login(credentials, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) ((Map) response.getBody()).get("success"));
        verify(session).setAttribute("username", "testuser");
        verify(userService).updateUserOnlineStatus("testuser", true);
    }

    @Test
    void testLogin_Failure() {
        // 路径覆盖：测试登录失败路径
        when(userService.validateUser("testuser", "password123")).thenReturn(false);

        ResponseEntity<?> response = authController.login(credentials, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) ((Map) response.getBody()).get("success"));
        verify(session, never()).setAttribute(anyString(), any());
        verify(userService, never()).updateUserOnlineStatus(anyString(), anyBoolean());
    }

    @Test
    void testLogin_MissingUsername() {
        // 路径覆盖：测试缺少用户名路径
        credentials.remove("username");

        ResponseEntity<?> response = authController.login(credentials, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) ((Map) response.getBody()).get("success"));
    }

    @Test
    void testLogin_MissingPassword() {
        // 路径覆盖：测试缺少密码路径
        credentials.remove("password");

        ResponseEntity<?> response = authController.login(credentials, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) ((Map) response.getBody()).get("success"));
    }
}
