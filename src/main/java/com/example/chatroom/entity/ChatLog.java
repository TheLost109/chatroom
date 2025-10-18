package com.example.chatroom.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "chatlog")
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user", nullable = false)
    private String fromUser;

    @Column(name = "to_user", nullable = false)
    private String toUser;

    @Column(name = "message_type", length = 20)
    private String messageType = "text";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read")
    private Boolean isRead = false;

    // 构造函数
    public ChatLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatLog(String fromUser, String toUser, String content) {
        this();
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
    }
}
