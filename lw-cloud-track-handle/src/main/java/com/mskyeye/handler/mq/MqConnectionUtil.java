package com.mskyeye.handler.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @ClassName:MqConnectionUtil
 * @Description:消息队列连接工具
 * @Author:R.Gong
 * @Date:2022/11/17 14:01
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
@Data
public class MqConnectionUtil {

    @Value("${mq_ip}")
    private String mqAddr;

    @Value("${mq_port}")
    private Integer mqPort;

    @Value("${mq_username}")
    private String mqUserName;

    @Value("${mq_password}")
    private String mqPassWord;

    private Connection mqConn;
    private Channel channel;

    public static final String EXCHANGE_NAME = "iot.data";
    public static final String TRACK_QUEUE_NAME = "track";
    public static final String TRACK_QUEUE_ROUTING_KEY = "track.#";
    public static final String PRO_TRACK_QUEUE_NAME1 = "processed_track2trace";
    public static final String PRO_TRACK_QUEUE_NAME2 = "processed_track2ws";
    public static final String PRO_TRACK_QUEUE_NAME3 = "processed_track2db";
    public static final String PRO_TRACK_QUEUE_ROUTING_KEY1 = "processed_track2trace.#";
    public static final String PRO_TRACK_QUEUE_ROUTING_KEY2 = "processed_track2ws.#";
    public static final String PRO_TRACK_QUEUE_ROUTING_KEY3 = "processed_track2db.#";
    public static final String CAR_TRACK_QUEUE_NAME = "car_track";
    public static final String CAR_TRACK_QUEUE_ROUTING_KEY = "car_track.#";
    public static final String NEW_ALARM_QUEUE_NAME = "new_alarm";
    public static final String NEW_ALARM_QUEUE_ROUTING_KEY = "new_alarm.#";
    public Boolean initMqConfig() {
        try {
            //定义连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置服务地址
            factory.setHost(this.getMqAddr());
            //端口
            factory.setPort(mqPort);
            //设置账号信息，用户名、密码、vhost
            factory.setUsername(mqUserName);
            factory.setPassword(mqPassWord);
            factory.setAutomaticRecoveryEnabled(true); // 开启自动重连功能
            factory.setNetworkRecoveryInterval(1000); // 自动重连间隔时间，单位为毫秒
            // 获取连接
            Connection connection = factory.newConnection();
            mqConn = connection;
            channel = mqConn.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            // 声明队列
            channel.queueDeclare(TRACK_QUEUE_NAME, true, false, false, null);
            channel.queueDeclare(PRO_TRACK_QUEUE_NAME1, true, false, false, null);
            channel.queueDeclare(PRO_TRACK_QUEUE_NAME2, true, false, false, null);
            channel.queueDeclare(PRO_TRACK_QUEUE_NAME3, true, false, false, null);
            channel.queueDeclare(CAR_TRACK_QUEUE_NAME, true, false, false, null);
            channel.queueDeclare(NEW_ALARM_QUEUE_NAME, true, false, false, null);

            // 将队列与交换机通过路由键绑定
            channel.queueBind(TRACK_QUEUE_NAME, EXCHANGE_NAME, TRACK_QUEUE_ROUTING_KEY);
            channel.queueBind(PRO_TRACK_QUEUE_NAME1, EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1);
            channel.queueBind(PRO_TRACK_QUEUE_NAME2, EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2);
            channel.queueBind(PRO_TRACK_QUEUE_NAME3, EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3);
            channel.queueBind(CAR_TRACK_QUEUE_NAME, EXCHANGE_NAME, CAR_TRACK_QUEUE_ROUTING_KEY);
            channel.queueBind(NEW_ALARM_QUEUE_NAME, EXCHANGE_NAME, NEW_ALARM_QUEUE_ROUTING_KEY);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【track_handle服务的MQ连接】失败", e.getMessage());
            return false;
        }
    }
}
