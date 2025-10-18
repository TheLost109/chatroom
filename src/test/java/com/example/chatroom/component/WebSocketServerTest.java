package com.example.chatroom.component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.service.ChatLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebSocketServerTest {

    @Mock
    private ChatLogService chatLogService;

    @Mock
    private Session session1;

    @Mock
    private Session session2;

    @Mock
    private RemoteEndpoint.Basic remoteEndpoint1;

    @Mock
    private RemoteEndpoint.Basic remoteEndpoint2;

    @InjectMocks
    private WebSocketServer webSocketServer;

    @BeforeEach
    void setUp() throws IOException {
        // 初始化会话映射
        WebSocketServer.sessionMap.clear();

        // 配置模拟会话
        when(session1.getId()).thenReturn("session1");
        when(session1.getBasicRemote()).thenReturn(remoteEndpoint1);
        when(session1.isOpen()).thenReturn(true);

        when(session2.getId()).thenReturn("session2");
        when(session2.getBasicRemote()).thenReturn(remoteEndpoint2);
        when(session2.isOpen()).thenReturn(true);

        // 设置ChatLogService静态引用
        WebSocketServer.setChatLogService(chatLogService);
    }

    // 条件覆盖测试用例
    @Test
    void testOnMessage_ChatMessageToOnlineUser() throws IOException {
        // 条件覆盖：消息类型=chat，目标用户在线
        WebSocketServer.sessionMap.put("user1", session1);
        WebSocketServer.sessionMap.put("user2", session2);

        String message = "{\"to\": \"user2\", \"text\": \"Hello\", \"type\": \"chat\"}";

        webSocketServer.onMessage(message, session1, "user1");

        verify(chatLogService).saveMessage("user1", "user2", "Hello");
        verify(remoteEndpoint2).sendText(contains("Hello"));
    }

    @Test
    void testOnMessage_ChatMessageToOfflineUser() throws IOException {
        // 条件覆盖：消息类型=chat，目标用户离线
        WebSocketServer.sessionMap.put("user1", session1);

        String message = "{\"to\": \"user2\", \"text\": \"Hello\", \"type\": \"chat\"}";

        webSocketServer.onMessage(message, session1, "user1");

        verify(chatLogService).saveMessage("user1", "user2", "Hello");
        verify(remoteEndpoint1).sendText(contains("不在线"));
    }

    @Test
    void testOnMessage_BroadcastMessage() throws IOException {
        // 条件覆盖：消息类型=broadcast
        WebSocketServer.sessionMap.put("user1", session1);
        WebSocketServer.sessionMap.put("user2", session2);

        String message = "{\"text\": \"Broadcast\", \"type\": \"broadcast\"}";

        webSocketServer.onMessage(message, session1, "user1");

        verify(remoteEndpoint1).sendText(contains("Broadcast"));
        verify(remoteEndpoint2).sendText(contains("Broadcast"));
        verify(chatLogService, never()).saveMessage(any(), any(), any());
    }

    @Test
    void testOnMessage_InvalidMessageFormat() {
        // 条件覆盖：消息格式无效
        String invalidMessage = "invalid json";

        webSocketServer.onMessage(invalidMessage, session1, "user1");

        verify(chatLogService, never()).saveMessage(any(), any(), any());
        verifyNoInteractions(remoteEndpoint1);
        verifyNoInteractions(remoteEndpoint2);
    }

    // 判定覆盖测试用例
    @Test
    void testOnMessage_MessageProcessingException() throws IOException {
        // 判定覆盖：消息处理过程中发生异常
        WebSocketServer.sessionMap.put("user1", session1);
        WebSocketServer.sessionMap.put("user2", session2);

        String message = "{\"to\": \"user2\", \"text\": \"Hello\", \"type\": \"chat\"}";

        // 模拟发送消息时发生异常
        doThrow(new IOException("Network error")).when(remoteEndpoint2).sendText(anyString());

        webSocketServer.onMessage(message, session1, "user1");

        // 验证即使发送失败，消息仍被保存
        verify(chatLogService).saveMessage("user1", "user2", "Hello");
    }
}