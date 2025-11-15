package com.mskyeye.ws.mq.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 优化后的 MQTT 客户端管理类
 * - 保留会话 cleanSession=false
 * - 高频消息入队列异步发布
 * - 自动重连
 */
@Slf4j
@Component
public class MqttClientManager {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.keepAliveInterval}")
    private int keepAliveInterval;

    @Value("${mqtt.connectionTimeout}")
    private int connectionTimeout;

    @Value("${mqtt.reconnectDelay}")
    private long reconnectDelay; // 毫秒

    private MqttClient client;
    private final Object lock = new Object();
    private final ConcurrentLinkedQueue<MqttMessageWrapper> messageQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean connecting = false;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @PostConstruct
    public void init() {
        // 自动连接线程
        executor.scheduleWithFixedDelay(this::connectWithRetry, 0, reconnectDelay, TimeUnit.MILLISECONDS);
        // 队列消息发布线程
        executor.scheduleWithFixedDelay(this::flushQueue, 100, 50, TimeUnit.MILLISECONDS);
    }

    private void connectWithRetry() {
        if (client != null && client.isConnected()) return;

        synchronized (lock) {
            if (connecting) return;
            connecting = true;
        }

        try {
            if (client == null) {
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        log.warn("MQTT 连接断开，等待自动重连", cause);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {}

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false); // 保留会话
            options.setAutomaticReconnect(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setKeepAliveInterval(keepAliveInterval);
            options.setConnectionTimeout(connectionTimeout);

            log.info("MQTT 开始连接：{}", broker);
            client.connect(options);
            log.info("MQTT 连接成功！");
        } catch (MqttException e) {
            log.error("MQTT 连接失败", e);
        } finally {
            connecting = false;
        }
    }

    private void flushQueue() {
        if (client == null || !client.isConnected()) return;

        while (!messageQueue.isEmpty()) {
            MqttMessageWrapper wrapper = messageQueue.poll();
            if (wrapper != null) {
                try {
                    MqttMessage msg = new MqttMessage(wrapper.payload.getBytes());
                    msg.setQos(wrapper.qos);
                    client.publish(wrapper.topic, msg);
                    log.info("MQTT 队列消息发布成功 -> Topic: {}, Payload: {}", wrapper.topic, wrapper.payload);
                } catch (MqttException e) {
                    log.error("队列消息发布失败，重新入队", e);
                    messageQueue.offer(wrapper); // 重试
                    break; // 避免快速循环
                }
            }
        }
    }

    /**
     * 高频调用只入队列，不直接触发连接
     */
    public void publish(String topic, String payload, int qos) {
        messageQueue.offer(new MqttMessageWrapper(topic, payload, qos));
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                log.info("已断开 MQTT 连接");
            }
        } catch (MqttException e) {
            log.error("断开 MQTT 连接出错", e);
        } finally {
            executor.shutdown();
        }
    }

    private static class MqttMessageWrapper {
        final String topic;
        final String payload;
        final int qos;

        MqttMessageWrapper(String topic, String payload, int qos) {
            this.topic = topic;
            this.payload = payload;
            this.qos = qos;
        }
    }
}
