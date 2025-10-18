package com.example.chatroom.controller;

import com.example.chatroom.entity.User;
import com.example.chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 允许Vue前端跨域访问
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("data", registeredUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (userService.validateUser(username, password)) {
            // 设置会话属性
            session.setAttribute("username", username);
            userService.updateUserOnlineStatus(username, true);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("username", username);

            // 可以返回一个token
            response.put("token", session.getId());

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            userService.updateUserOnlineStatus(username, false);
            session.invalidate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "退出成功");

            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "用户未登录");

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        String username = (String) session.getAttribute("username");

        Map<String, Object> response = new HashMap<>();
        if (username != null) {
            response.put("authenticated", true);
            response.put("username", username);
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }
}
