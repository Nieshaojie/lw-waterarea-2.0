package com.mskyeye.ws.mq.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * MQTT 客户端连接管理类
 * 负责连接、重连、消息发布
 * 使用 Eclipse Paho 库实现，线程安全
 */
@Slf4j
@Component
public class MqttClientManager {

    // 从配置文件注入 MQTT 参数
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

    private MqttClient client; // 客户端对象实例

    /**
     * Spring 容器启动后自动执行
     * 负责初始化 MQTT 连接
     */
    @PostConstruct
    public void connect() {
        try {
            // 创建客户端对象（内存持久化）
            client = new MqttClient(broker, clientId, new MemoryPersistence());

            // 设置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);                // 不保留历史会话
            options.setUserName(username);                // 用户名
            options.setPassword(password.toCharArray());  // 密码
            options.setAutomaticReconnect(true);          // 启用自动重连
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);

            log.info("正在连接到 MQTT 服务器：{}", broker);
            client.connect(options); // 建立连接
            log.info("MQTT 连接成功！");
        } catch (MqttException e) {
            log.error("MQTT 连接失败，准备重连", e);
            scheduleReconnect();
        }
    }

    /**
     * 定时重连逻辑（若首次连接失败）
     */
    private void scheduleReconnect() {
        new Thread(() -> {
            while (client == null || !client.isConnected()) {
                try {
                    log.warn("尝试重新连接 MQTT 服务器...");
                    connect();
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error("重连失败", e);
                }
            }
        }, "mqtt-reconnect-thread").start();
    }

    /**
     * 判断客户端是否已连接
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * 发布 MQTT 消息（线程安全）
     * @param topic   发布主题
     * @param payload 消息内容（JSON 字符串）
     * @param qos     服务质量等级
     */
    public void publish(String topic, String payload, int qos) {
        try {
            if (!isConnected()) {
                log.warn("MQTT 未连接，自动重连中...");
                connect();
            }
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos); // 设置 QoS
            client.publish(topic, message);
            log.info("MQTT 发布成功 -> Topic: {}, Payload: {}", topic, payload);
        } catch (MqttException e) {
            log.error("MQTT 发布失败", e);
        }
    }

    /**
     * 程序关闭时安全断开连接
     */
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
        }
    }
}
