package com.example.chatroom.service;

import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.repository.ChatLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatLogServiceTest {

    @Mock
    private ChatLogRepository chatLogRepository;

    @InjectMocks
    private ChatLogService chatLogService;

    private ChatLog chatLog1;
    private ChatLog chatLog2;
    private List<ChatLog> chatLogs;

    @BeforeEach
    void setUp() {
        chatLog1 = new ChatLog("user1", "user2", "Hello");
        chatLog1.setId(1L);
        chatLog1.setTimestamp(LocalDateTime.now().minusMinutes(10));

        chatLog2 = new ChatLog("user2", "user1", "Hi there");
        chatLog2.setId(2L);
        chatLog2.setTimestamp(LocalDateTime.now().minusMinutes(5));

        chatLogs = Arrays.asList(chatLog1, chatLog2);
    }

    // 基本路径测试用例
    @Test
    void testGetChatHistory_BasicPath() {
        // 基本路径：正常查询聊天历史
        when(chatLogRepository.findChatHistory("user1", "user2"))
                .thenReturn(chatLogs);

        List<ChatLog> result = chatLogService.getChatHistory("user1", "user2");

        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("Hi there", result.get(1).getContent());
        verify(chatLogRepository).findChatHistory("user1", "user2");
    }

    @Test
    void testGetChatHistory_Paginated() {
        // 基本路径：分页查询聊天历史
        Page<ChatLog> page = new PageImpl<>(chatLogs);
        when(chatLogRepository.findChatHistory(eq("user1"), eq("user2"), any(Pageable.class)))
                .thenReturn(page);

        Page<ChatLog> result = chatLogService.getChatHistory("user1", "user2", 0, 20);

        assertEquals(2, result.getContent().size());
        verify(chatLogRepository).findChatHistory(eq("user1"), eq("user2"), any(Pageable.class));
    }

    // 条件覆盖测试用例
    @Test
    void testMarkAsRead_SingleMessage() {
        // 条件覆盖：标记单条消息为已读
        when(chatLogRepository.findById(1L)).thenReturn(Optional.of(chatLog1));
        when(chatLogRepository.save(any(ChatLog.class))).thenReturn(chatLog1);

        chatLogService.markAsRead(1L);

        assertTrue(chatLog1.getIsRead());
        verify(chatLogRepository).findById(1L);
        verify(chatLogRepository).save(chatLog1);
    }

    @Test
    void testMarkAsRead_MessageNotFound() {
        // 条件覆盖：消息不存在
        when(chatLogRepository.findById(999L)).thenReturn(Optional.empty());

        chatLogService.markAsRead(999L);

        verify(chatLogRepository).findById(999L);
        verify(chatLogRepository, never()).save(any());
    }

    @Test
    void testMarkAsRead_UserToUser() {
        // 条件覆盖：标记用户间所有消息为已读
        List<ChatLog> unreadMessages = Arrays.asList(chatLog1, chatLog2);
        when(chatLogRepository.findByFromUserAndToUserAndIsReadFalse("user1", "user2"))
                .thenReturn(unreadMessages);
        when(chatLogRepository.saveAll(anyList())).thenReturn(unreadMessages);

        chatLogService.markAsRead("user1", "user2");

        assertTrue(chatLog1.getIsRead());
        assertTrue(chatLog2.getIsRead());
        verify(chatLogRepository).findByFromUserAndToUserAndIsReadFalse("user1", "user2");
        verify(chatLogRepository).saveAll(unreadMessages);
    }

    // 边界条件测试
    @Test
    void testSaveMessage_EmptyContent() {
        // 边界条件：空消息内容
        ChatLog emptyChatLog = new ChatLog("user1", "user2", "");
        when(chatLogRepository.save(any(ChatLog.class))).thenReturn(emptyChatLog);

        ChatLog result = chatLogService.saveMessage(emptyChatLog);

        assertNotNull(result);
        assertEquals("", result.getContent());
        verify(chatLogRepository).save(emptyChatLog);
    }

    @Test
    void testCountUnreadMessages_NoUnread() {
        // 边界条件：没有未读消息
        when(chatLogRepository.countUnreadMessages("user1")).thenReturn(0L);

        Long count = chatLogService.countUnreadMessages("user1");

        assertEquals(0L, count);
        verify(chatLogRepository).countUnreadMessages("user1");
    }
}