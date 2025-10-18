package com.example.chatroom.repository;

import com.example.chatroom.entity.ChatLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

    // 查找两个用户之间的聊天记录
    @Query("SELECT c FROM ChatLog c WHERE " +
            "(c.fromUser = :user1 AND c.toUser = :user2) OR " +
            "(c.fromUser = :user2 AND c.toUser = :user1) " +
            "ORDER BY c.timestamp ASC")
    List<ChatLog> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);

    // 查找两个用户之间的聊天记录（分页）
    @Query("SELECT c FROM ChatLog c WHERE " +
            "(c.fromUser = :user1 AND c.toUser = :user2) OR " +
            "(c.fromUser = :user2 AND c.toUser = :user1) " +
            "ORDER BY c.timestamp DESC")
    Page<ChatLog> findChatHistory(@Param("user1") String user1,
                                  @Param("user2") String user2,
                                  Pageable pageable);

    // 查找用户的所有未读消息
    List<ChatLog> findByToUserAndIsReadFalse(String toUser);

    // 查找用户与特定用户的所有未读消息
    List<ChatLog> findByFromUserAndToUserAndIsReadFalse(String fromUser, String toUser);

    // 统计用户的未读消息数量
    @Query("SELECT COUNT(c) FROM ChatLog c WHERE c.toUser = :username AND c.isRead = false")
    Long countUnreadMessages(@Param("username") String username);
}
