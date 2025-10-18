package com.example.chatroom.repository;

import com.example.chatroom.entity.ChatLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.jdbc.batch_size=50",
        "spring.jpa.properties.hibernate.order_inserts=true",
        "spring.jpa.properties.hibernate.order_updates=true"
})
public class DatabasePerformanceTest {

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Test
    void testBatchInsertPerformance() {
        // 性能测试：批量插入性能
        int batchSize = 1000;
        List<ChatLog> chatLogs = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < batchSize; i++) {
            ChatLog chatLog = new ChatLog();
            chatLog.setFromUser("user1");
            chatLog.setToUser("user2");
            chatLog.setContent("Performance test message " + i);
            chatLog.setTimestamp(LocalDateTime.now().minusMinutes(i));
            chatLogs.add(chatLog);
        }

        List<ChatLog> savedLogs = chatLogRepository.saveAll(chatLogs);
        chatLogRepository.flush();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(batchSize, savedLogs.size());

        // 性能断言：1000条记录应在5秒内完成
        assertTrue(duration < 5000,
                "Batch insert took too long: " + duration + "ms for " + batchSize + " records");

        System.out.println("Batch insert performance: " + batchSize +
                " records in " + duration + "ms");
    }

    @Test
    void testQueryPerformanceWithIndex() {
        // 性能测试：索引查询性能
        // 先创建测试数据
        for (int i = 0; i < 100; i++) {
            ChatLog chatLog = new ChatLog();
            chatLog.setFromUser("queryuser");
            chatLog.setToUser("targetuser");
            chatLog.setContent("Query test message " + i);
            chatLog.setTimestamp(LocalDateTime.now().minusHours(i));
            chatLogRepository.save(chatLog);
        }
        chatLogRepository.flush();

        // 测试查询性能
        long startTime = System.currentTimeMillis();

        List<ChatLog> history = chatLogRepository.findChatHistory("queryuser", "targetuser");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(100, history.size());

        // 性能断言：100条记录查询应在100ms内完成
        assertTrue(duration < 100,
                "Indexed query took too long: " + duration + "ms for 100 records");

        System.out.println("Indexed query performance: 100 records in " + duration + "ms");
    }
}
