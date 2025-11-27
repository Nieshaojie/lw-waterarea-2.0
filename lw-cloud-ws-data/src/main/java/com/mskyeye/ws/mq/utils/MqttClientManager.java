/*
package com.mskyeye.ws.mq.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

*/
/**
 * 稳定版 MQTT 客户端管理类
 * - 使用 Paho 内置自动重连（无重连竞争）
 * - cleanSession=false 保留会话
 * - 高频消息入队列异步发布
 *//*

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

    private MqttClient client;
    private final ConcurrentLinkedQueue<MqttMessageWrapper> messageQueue = new ConcurrentLinkedQueue<>();

    */
/** 队列发布线程（保留） *//*

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 连接断开，将由 Paho 自动重连", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {}

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false); // 保持会话
            options.setAutomaticReconnect(true); // 使用 Paho 自动重连
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setKeepAliveInterval(keepAliveInterval);
            options.setConnectionTimeout(connectionTimeout);

            log.info("MQTT 正在连接：{}", broker);
            client.connect(options);
            log.info("MQTT 连接成功");

        } catch (Exception e) {
            log.error("MQTT 首次连接失败", e);
        }

        // 队列消息发布线程
        executor.scheduleWithFixedDelay(this::flushQueue, 100, 50, TimeUnit.MILLISECONDS);
    }

    */
/**
     * 异步发布队列
     *//*

    private void flushQueue() {
        if (client == null || !client.isConnected()) {
            return;
        }

        while (!messageQueue.isEmpty()) {
            MqttMessageWrapper wrapper = messageQueue.poll();
            if (wrapper != null) {
                try {
                    MqttMessage msg = new MqttMessage(wrapper.payload.getBytes());
                    msg.setQos(wrapper.qos);
                    client.publish(wrapper.topic, msg);
                    //log.info("MQTT 发布成功 -> Topic: {}, Payload: {}", wrapper.topic, wrapper.payload);
                } catch (MqttException e) {
                    log.error("队列消息发布失败，重新入队", e);
                    messageQueue.offer(wrapper);
                    break;
                }
            }
        }
    }

    */
/**
     * 高频发布 → 入队，不阻塞
     *//*

    public void publish(String topic, String payload, int qos) {
        messageQueue.offer(new MqttMessageWrapper(topic, payload, qos));
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                log.info("MQTT 已断开");
            }
        } catch (MqttException e) {
            log.error("断开 MQTT 连接出错", e);
        } finally {
            executor.shutdown();
        }
    }

    */
/**
     * 队列包装
     *//*

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
*/
