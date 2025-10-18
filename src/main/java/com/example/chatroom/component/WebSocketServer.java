package com.example.chatroom.component;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatroom.entity.ChatLog;
import com.example.chatroom.service.ChatLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/imserver/{username}")
@Component
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private static ChatLogService chatLogService;

//    @Autowired
//    public void setChatLogService(ChatLogService chatLogService) {
//        WebSocketServer.chatLogService = chatLogService;
//    }
    @Autowired
    public static void setChatLogService(ChatLogService chatLogService) {
        WebSocketServer.chatLogService = chatLogService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessionMap.put(username, session);
        log.info("有新用户加入，username={}，当前在线人数为={}", username, sessionMap.size());

        // 广播用户上线消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("type", "system");
        systemMessage.set("content", "用户 " + username + " 加入了聊天室");
        systemMessage.set("onlineCount", sessionMap.size());
        sendAllMessage(JSONUtil.toJsonStr(systemMessage));

        // 广播更新用户列表
        broadcastUserList();
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessionMap.remove(username);
        log.info("有一连接关闭，移除username={}的用户session，当前在线人数为：{}", username, sessionMap.size());

        // 广播用户下线消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("type", "system");
        systemMessage.set("content", "用户 " + username + " 离开了聊天室");
        systemMessage.set("onlineCount", sessionMap.size());
        sendAllMessage(JSONUtil.toJsonStr(systemMessage));

        // 广播更新用户列表
        broadcastUserList();
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("username") String username) {
        log.info("服务端收到用户username={}的消息：{}", username, message);

        try {
            JSONObject obj = JSONUtil.parseObj(message);
            String toUsername = obj.getStr("to");
            String text = obj.getStr("text");
            String type = obj.getStr("type", "chat");

            // 保存聊天记录到数据库
            if (chatLogService != null && "chat".equals(type)) {
                ChatLog chatLog = new ChatLog(username, toUsername, text);
                chatLogService.saveMessage(chatLog);
                log.info("已保存聊天记录: from={}, to={}", username, toUsername);
            }

            if ("chat".equals(type)) {
                // 私聊消息
                Session toSession = sessionMap.get(toUsername);
                if (toSession != null && toSession.isOpen()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.set("from", username);
                    jsonObject.set("to", toUsername);
                    jsonObject.set("text", text);
                    jsonObject.set("type", "chat");
                    jsonObject.set("timestamp", System.currentTimeMillis());
                    this.sendMessage(jsonObject.toString(), toSession);
                    log.info("发送给用户username={}，消息={}", toUsername, jsonObject.toString());
                } else {
                    log.info("发送失败，未找到用户username={}的session或连接已关闭", toUsername);

                    // 可以返回一个错误消息给发送者
                    JSONObject errorMsg = new JSONObject();
                    errorMsg.set("type", "error");
                    errorMsg.set("content", "用户 " + toUsername + " 不在线");
                    this.sendMessage(errorMsg.toString(), session);
                }
            } else if ("broadcast".equals(type)) {
                // 广播消息
                JSONObject broadcastMsg = new JSONObject();
                broadcastMsg.set("from", username);
                broadcastMsg.set("text", text);
                broadcastMsg.set("type", "broadcast");
                broadcastMsg.set("timestamp", System.currentTimeMillis());
                sendAllMessage(JSONUtil.toJsonStr(broadcastMsg));
            }
        } catch (Exception e) {
            log.error("处理消息时发生错误", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    private void sendAllMessage(String message) {
        try {
            for (Session session : sessionMap.values()) {
                if (session.isOpen()) {
                    log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    private void broadcastUserList() {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.set("type", "userlist");
        result.set("users", array);

        for (String username : sessionMap.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("username", username);
            array.add(jsonObject);
        }

        sendAllMessage(JSONUtil.toJsonStr(result));
    }
}
