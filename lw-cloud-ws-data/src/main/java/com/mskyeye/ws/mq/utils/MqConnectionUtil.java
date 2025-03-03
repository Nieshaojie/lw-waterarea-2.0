package com.mskyeye.ws.mq.utils;

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
    public static final String TRACK_QUEUE_NAME = "processed_track2ws";
    public static final String TRACK_QUEUE_ROUTING_KEY = "processed_track2ws.#";

    public static final String CAMERA_STATUS_QUEUE_NAME = "camera_status";
    public static final String CAMERA_STATUS_QUEUE_ROUTING_KEY = "camera_status.#";

    public static final String AI_ALARM_EVENT_QUEUE_NAME = "ai_alarm";
    public static final String AI_ALARM_EVENT_QUEUE_ROUTING_KEY = "ai_alarm.#";

    public static final String CAR_TRACK_QUEUE_NAME = "car_track";
    public static final String CAR_TRACK_QUEUE_ROUTING_KEY = "car_track.#";
    public Boolean initMqConfig(){
        try {
            //定义连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置服务地址
            factory.setHost(mqAddr);
            //端口
            factory.setPort(mqPort);
            //设置账号信息，用户名、密码、vhost
//            factory.setVirtualHost("/iot");
            factory.setUsername(mqUserName);
            factory.setPassword(mqPassWord);
            factory.setAutomaticRecoveryEnabled(true); // 开启自动重连功能
            factory.setNetworkRecoveryInterval(1000); // 自动重连间隔时间，单位为毫秒
            // 获取连接
            Connection connection = factory.newConnection();
            mqConn = connection;
            channel = mqConn.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            // 创建参数map，并设置x-expires参数为-1
//            Map<String, Object> arguments = new HashMap<>();
//            arguments.put("x-expires", -1);
            // 航迹队列
            channel.queueDeclare(TRACK_QUEUE_NAME, true, false, false, null);
            channel.queueBind(TRACK_QUEUE_NAME, EXCHANGE_NAME, TRACK_QUEUE_ROUTING_KEY);

            // 相机状态队列
            channel.queueDeclare(CAMERA_STATUS_QUEUE_NAME, true, false, false, null);
            channel.queueBind(CAMERA_STATUS_QUEUE_NAME, EXCHANGE_NAME, CAMERA_STATUS_QUEUE_ROUTING_KEY);

            // AI告警事件队列
            channel.queueDeclare(AI_ALARM_EVENT_QUEUE_NAME, true, false, false, null);
            channel.queueBind(AI_ALARM_EVENT_QUEUE_NAME, EXCHANGE_NAME, AI_ALARM_EVENT_QUEUE_ROUTING_KEY);

            // 车载GPS信息队列
            channel.queueDeclare(CAR_TRACK_QUEUE_NAME, true, false, false, null);
            channel.queueBind(CAR_TRACK_QUEUE_NAME, EXCHANGE_NAME, CAR_TRACK_QUEUE_ROUTING_KEY);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("【ws服务的MQ连接】失败",e.getMessage());
            return false;
        }
    }
}
