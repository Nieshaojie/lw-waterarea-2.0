package com.mskyeye.shipData.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @ClassName:AisMqConnUtil
 * @Description:AIS静态数据连接工具
 * @Author:R.Gong
 * @Date:2023/5/25 11:02
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
@Data
public class AisMqConnUtil {

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

    public static String QUEUE_NAME = "ais.static.data";

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
            // 获取连接
            Connection connection = factory.newConnection();
            mqConn = connection;
            channel = mqConn.createChannel();
            // 声明队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error("【ais.mq服务的MQ连接】失败",e.getMessage());
            return false;
        }
    }
}
