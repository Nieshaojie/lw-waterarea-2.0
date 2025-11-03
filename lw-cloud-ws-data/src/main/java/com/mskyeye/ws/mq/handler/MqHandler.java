package com.mskyeye.ws.mq.handler;

import com.alibaba.fastjson2.JSON;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.ws.mq.utils.MqConnectionUtil;
import com.mskyeye.ws.mq.utils.MqttMessageSender;
import com.mskyeye.ws.server.WebSocketServer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName:MqHandler
 * @Description:消息队列处理器
 * @Author:R.Gong
 * @Date:2022/11/21 17:20
 * @Version:1.0
 **/
@Component
@Slf4j
@RefreshScope
public class MqHandler implements ApplicationRunner {

    @Autowired
    private MqConnectionUtil mqConnUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private MqttMessageSender mqttMessageSender;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化消息队列配置
        mqConnUtil.initMqConfig();
        // 航迹队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(mqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body, CharsetUtil.UTF_8);
                //log.info("收到航迹信息：{}",msg);
                webSocketServer.sendTrackMsgToAll(msg);

                // 2. 转发到苏州融合效能测试平台 MQTT
                try {
                    LwTrackPacket packet = JSON.parseObject(msg, LwTrackPacket.class);
                    if(packet.getITEM().get(0).getSOURCE() == 2) {
                        mqttMessageSender.sendTrackToPlatform(packet);
                    }
                } catch (Exception e) {
                    log.error("航迹数据解析或转发失败", e);
                }
            }
        };
        // 监听队列，自动返回完成
        mqConnUtil.getChannel().basicConsume(MqConnectionUtil.TRACK_QUEUE_NAME, true, consumer);

        // 相机状态队列的消费者
        DefaultConsumer consumer1 = new DefaultConsumer(mqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body, CharsetUtil.UTF_8);
                webSocketServer.sendCameraStatusToAll(msg);
            }
        };
        // 监听队列，自动返回完成
        mqConnUtil.getChannel().basicConsume(MqConnectionUtil.CAMERA_STATUS_QUEUE_NAME, true, consumer1);

        // AI告警事件队列的消费者
        DefaultConsumer consumer2 = new DefaultConsumer(mqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body, CharsetUtil.UTF_8);
                webSocketServer.sendAiAlarmEventToAll(msg);
            }
        };
        // 监听队列，自动返回完成
        mqConnUtil.getChannel().basicConsume(MqConnectionUtil.AI_ALARM_EVENT_QUEUE_NAME, true, consumer2);

        // 车载GPS信息队列的消费者
        DefaultConsumer consumer3 = new DefaultConsumer(mqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body, CharsetUtil.UTF_8);
                webSocketServer.sendCarInfoToAll(msg);
            }
        };
        // 监听队列，自动返回完成
        mqConnUtil.getChannel().basicConsume(MqConnectionUtil.CAR_TRACK_QUEUE_NAME, true, consumer3);
    }
}
