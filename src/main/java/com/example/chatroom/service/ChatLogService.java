package com.example.chatroom.service;

import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.repository.ChatLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLogService {

    @Autowired
    private ChatLogRepository chatLogRepository;

    // 保存聊天记录
    public ChatLog saveMessage(ChatLog chatLog) {
        return chatLogRepository.save(chatLog);
    }

    // 保存聊天记录（简化方法）
    public ChatLog saveMessage(String fromUser, String toUser, String content) {
        ChatLog chatLog = new ChatLog(fromUser, toUser, content);
        return chatLogRepository.save(chatLog);
    }

    // 获取两个用户之间的聊天历史
    public List<ChatLog> getChatHistory(String user1, String user2) {
        return chatLogRepository.findChatHistory(user1, user2);
    }

    // 获取两个用户之间的聊天历史（分页）
    public Page<ChatLog> getChatHistory(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return chatLogRepository.findChatHistory(user1, user2, pageable);
    }

    // 获取用户的未读消息
    public List<ChatLog> getUnreadMessages(String username) {
        return chatLogRepository.findByToUserAndIsReadFalse(username);
    }

    // 获取用户与特定用户的未读消息
    public List<ChatLog> getUnreadMessages(String fromUser, String toUser) {
        return chatLogRepository.findByFromUserAndToUserAndIsReadFalse(fromUser, toUser);
    }

    // 标记消息为已读
    public void markAsRead(Long messageId) {
        chatLogRepository.findById(messageId).ifPresent(chatLog -> {
            chatLog.setIsRead(true);
            chatLogRepository.save(chatLog);
        });
    }

    // 标记用户的所有消息为已读
    public void markAllAsRead(String username) {
        List<ChatLog> unreadMessages = chatLogRepository.findByToUserAndIsReadFalse(username);
        for (ChatLog message : unreadMessages) {
            message.setIsRead(true);
        }
        chatLogRepository.saveAll(unreadMessages);
    }

    // 标记与特定用户的消息为已读
    public void markAsRead(String fromUser, String toUser) {
        List<ChatLog> unreadMessages = chatLogRepository.findByFromUserAndToUserAndIsReadFalse(fromUser, toUser);
        for (ChatLog message : unreadMessages) {
            message.setIsRead(true);
        }
        chatLogRepository.saveAll(unreadMessages);
    }

    // 统计未读消息数量
    public Long countUnreadMessages(String username) {
        return chatLogRepository.countUnreadMessages(username);
    }
}