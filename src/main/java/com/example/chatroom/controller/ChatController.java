package com.example.chatroom.controller;

import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.service.ChatLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ChatController {

    @Autowired
    private ChatLogService chatLogService;

    // 获取与特定用户的聊天历史
    @GetMapping("/history/{username}")
    public ResponseEntity<?> getChatHistory(
            @PathVariable String username,
            @RequestParam String currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        try {
            Page<ChatLog> chatHistory = chatLogService.getChatHistory(currentUser, username, page, size);

            // 标记这些消息为已读
            chatLogService.markAsRead(username, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", chatHistory.getContent());
            response.put("totalPages", chatHistory.getTotalPages());
            response.put("totalElements", chatHistory.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取聊天记录失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取未读消息数量
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@RequestParam String username) {
        try {
            Long unreadCount = chatLogService.countUnreadMessages(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", unreadCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读消息数量失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取所有未读消息
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(@RequestParam String username) {
        try {
            List<ChatLog> unreadMessages = chatLogService.getUnreadMessages(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", unreadMessages);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读消息失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    // 标记消息为已读
    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markAsRead(@RequestBody Map<String, Object> request) {
        try {
            String fromUser = (String) request.get("fromUser");
            String toUser = (String) request.get("toUser");

            chatLogService.markAsRead(fromUser, toUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息已标记为已读");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "标记消息为已读失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
