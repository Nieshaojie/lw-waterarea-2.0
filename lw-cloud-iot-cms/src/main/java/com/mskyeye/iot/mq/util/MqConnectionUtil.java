package com.mskyeye.iot.mq.util;

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

    public static final String EXCHANGE_NAME = "iot.radarstation.data";
    public static final String TRACK_QUEUE_NAME = "track";
    public static final String TRACK_QUEUE_ROUTING_KEY = "track.#";
    public static final String AIS_STATIC_QUEUE_NAME = "ais.static.data";
    public static final String AIS_STATIC_QUEUE_ROUTING_KEY = "ais.static.data.#";

    public Boolean initMqConfig() {
        try {
            //定义连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置服务地址
            factory.setHost(this.getMqAddr());
            //端口
            factory.setPort(mqPort);
            //设置账号信息，用户名、密码、vhost
//            factory.setVirtualHost("/iot");
            factory.setUsername(mqUserName);
            factory.setPassword(mqPassWord);
            // 获取连接
            Connection connection = factory.newConnection();
            mqConn = connection;
            channel = mqConn.createChannel();
            // 定义交换机类型为主题交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            // 声明队列，如果队列不存在则创建
            channel.queueDeclare(TRACK_QUEUE_NAME, true, false, false, null);
            channel.queueDeclare(AIS_STATIC_QUEUE_NAME, true, false, false, null);

            // 将队列与交换机通过路由键绑定
            channel.queueBind(TRACK_QUEUE_NAME, EXCHANGE_NAME, TRACK_QUEUE_ROUTING_KEY);
            channel.queueBind(AIS_STATIC_QUEUE_NAME, EXCHANGE_NAME, AIS_STATIC_QUEUE_ROUTING_KEY);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【iot服务的MQ连接】失败", e.getMessage());
            return false;
        }
    }
}
