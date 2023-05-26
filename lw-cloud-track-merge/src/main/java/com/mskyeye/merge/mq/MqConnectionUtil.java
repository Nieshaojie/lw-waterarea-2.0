package com.mskyeye.merge.mq;

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

    private Channel originalTrackChannel;

    private Channel mergeTrackChannel;

    public static final String ORIGINAL_EXCHANGE_NAME = "iot.radarstation.data";

    public static final String MERGE_EXCHANGE_NAME = "merge_track";

    public Boolean initMqConfig(){
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

            originalTrackChannel = mqConn.createChannel();
            originalTrackChannel.exchangeDeclare(ORIGINAL_EXCHANGE_NAME,"fanout");

            mergeTrackChannel = mqConn.createChannel();
            mergeTrackChannel.exchangeDeclare(MERGE_EXCHANGE_NAME,"fanout");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("【merge服务的MQ连接】失败",e.getMessage());
            return false;
        }
    }
}
