package com.example.chatroom.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Column(unique = true)
    private String room_name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
