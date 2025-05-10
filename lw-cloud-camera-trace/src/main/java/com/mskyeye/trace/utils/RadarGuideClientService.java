package com.mskyeye.trace.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class RadarGuideClientService {

    @Value("${radar.ws-url}")
    private String wsUrl;

    private WebSocketClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean connected = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        connectWebSocket();
        startReconnectTask();
    }

    private void connectWebSocket() {
        try {
            URI serverUri = new URI(wsUrl);
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    connected = true;
                    System.out.println("[WebSocket] 已连接");
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("[WebSocket] 收到消息: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    System.out.println("[WebSocket] 连接关闭: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    connected = false;
                    System.err.println("[WebSocket] 错误: " + ex.getMessage());
                }
            };
            client.connect();
        } catch (Exception e) {
            System.err.println("[WebSocket] 连接失败: " + e.getMessage());
        }
    }

    private void startReconnectTask() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!connected || client == null || client.isClosed()) {
                System.out.println("[WebSocket] 检测到断线，尝试重连...");
                connectWebSocket();
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    public void sendStartGuide(Map<String, Object> payload) {
        send(payload);
    }

    public void sendStopGuide(Map<String, Object> payload) {

        send(payload);
    }

    private void send(Map<String, Object> payload) {
        if (connected && client != null && client.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(payload);
                client.send(message);
                System.out.println("[WebSocket] 已发送: " + message);
            } catch (Exception e) {
                System.err.println("[WebSocket] 发送失败: " + e.getMessage());
            }
        } else {
            System.err.println("[WebSocket] 未连接，发送被忽略");
        }
    }
}
