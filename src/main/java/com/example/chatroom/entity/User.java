package com.example.chatroom.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
//    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private Date created_time;

    private boolean online;

    // 构造函数
//    public User() {
//        this.created_time = new Date();
//        this.online = false;
//    }
//
//    public User(String username, String password, String email) {
//        this();
//        this.username = username;
//        this.password = password;
//    }
}
