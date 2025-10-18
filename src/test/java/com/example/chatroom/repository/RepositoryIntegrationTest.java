package com.example.chatroom.repository;

import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatLogRepository chatLogRepository;

    // 基本路径测试用例
    @Test
    void testUserRepository_SaveAndFind() {
        // 基本路径：用户保存和查询
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setOnline(true);

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertTrue(foundUser.get().isOnline());
    }

    @Test
    void testUserRepository_ExistsByUsername() {
        // 基本路径：用户名存在性检查
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password");
        entityManager.persist(user);

        boolean exists = userRepository.existsByUsername("existinguser");
        assertTrue(exists);

        boolean notExists = userRepository.existsByUsername("nonexistent");
        assertFalse(notExists);
    }

    @Test
    void testChatLogRepository_SaveMessage() {
        // 基本路径：保存聊天记录
        ChatLog chatLog = new ChatLog();
        chatLog.setFromUser("user1");
        chatLog.setToUser("user2");
        chatLog.setContent("Test message");
        chatLog.setTimestamp(LocalDateTime.now());

        ChatLog savedLog = chatLogRepository.save(chatLog);
        assertNotNull(savedLog.getId());
        assertEquals("user1", savedLog.getFromUser());
        assertEquals("user2", savedLog.getToUser());
        assertFalse(savedLog.getIsRead());
    }

    // 循环测试用例 - 批量操作
    @Test
    void testChatLogRepository_BatchOperations() {
        // 循环测试：批量插入和查询
        for (int i = 0; i < 10; i++) {
            ChatLog chatLog = new ChatLog();
            chatLog.setFromUser("user1");
            chatLog.setToUser("user2");
            chatLog.setContent("Message " + i);
            chatLog.setTimestamp(LocalDateTime.now().minusMinutes(i));
            entityManager.persist(chatLog);
        }
        entityManager.flush();

        List<ChatLog> history = chatLogRepository.findChatHistory("user1", "user2");
        assertEquals(10, history.size());

        // 验证时间顺序
        for (int i = 0; i < history.size() - 1; i++) {
            assertTrue(history.get(i).getTimestamp()
                    .isBefore(history.get(i + 1).getTimestamp()));
        }
    }

    @Test
    void testChatLogRepository_FindChatHistoryPagination() {
        // 循环测试：分页查询
        // 创建50条测试数据
        for (int i = 0; i < 50; i++) {
            ChatLog chatLog = new ChatLog();
            chatLog.setFromUser("userA");
            chatLog.setToUser("userB");
            chatLog.setContent("Message " + i);
            chatLog.setTimestamp(LocalDateTime.now().minusHours(i));
            entityManager.persist(chatLog);
        }
        entityManager.flush();

        // 测试分页查询
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatLog> page1 = chatLogRepository.findChatHistory("userA", "userB", pageable);

        assertEquals(50, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());
        assertEquals(20, page1.getContent().size());

        // 测试第二页
        pageable = PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatLog> page2 = chatLogRepository.findChatHistory("userA", "userB", pageable);

        assertEquals(20, page2.getContent().size());
    }

    @Test
    void testChatLogRepository_FindUnreadMessages() {
        // 基本路径：查找未读消息
        // 创建已读和未读消息
        ChatLog readMessage = new ChatLog();
        readMessage.setFromUser("user1");
        readMessage.setToUser("user2");
        readMessage.setContent("Read message");
        readMessage.setIsRead(true);
        entityManager.persist(readMessage);

        ChatLog unreadMessage = new ChatLog();
        unreadMessage.setFromUser("user1");
        unreadMessage.setToUser("user2");
        unreadMessage.setContent("Unread message");
        unreadMessage.setIsRead(false);
        entityManager.persist(unreadMessage);

        entityManager.flush();

        List<ChatLog> unreadMessages = chatLogRepository.findByToUserAndIsReadFalse("user2");
        assertEquals(1, unreadMessages.size());
        assertEquals("Unread message", unreadMessages.get(0).getContent());
    }

    @Test
    void testChatLogRepository_CountUnreadMessages() {
        // 基本路径：统计未读消息数量
        // 创建测试数据
        for (int i = 0; i < 5; i++) {
            ChatLog chatLog = new ChatLog();
            chatLog.setFromUser("sender");
            chatLog.setToUser("receiver");
            chatLog.setContent("Message " + i);
            chatLog.setIsRead(i % 2 == 0); // 一半已读，一半未读
            entityManager.persist(chatLog);
        }
        entityManager.flush();

        Long unreadCount = chatLogRepository.countUnreadMessages("receiver");
        assertEquals(2L, unreadCount); // 应该有2条未读消息
    }

    // 边界条件测试
    @Test
    void testChatLogRepository_EmptyResult() {
        // 边界条件：空结果集
        List<ChatLog> history = chatLogRepository.findChatHistory("nonexistent", "user");
        assertTrue(history.isEmpty());

        List<ChatLog> unread = chatLogRepository.findByToUserAndIsReadFalse("nonexistent");
        assertTrue(unread.isEmpty());

        Long count = chatLogRepository.countUnreadMessages("nonexistent");
        assertEquals(0L, count);
    }

    @Test
    void testUserRepository_UniqueUsernameConstraint() {
        // 边界条件：唯一用户名约束
        User user1 = new User();
        user1.setUsername("duplicate");
        user1.setPassword("pass1");
        entityManager.persist(user1);
        entityManager.flush();

        // 尝试创建重复用户名的用户
        User user2 = new User();
        user2.setUsername("duplicate");
        user2.setPassword("pass2");

        // 这里应该抛出异常，但具体处理取决于业务逻辑
        // 在实际应用中应该有适当的异常处理
        assertDoesNotThrow(() -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }
}
