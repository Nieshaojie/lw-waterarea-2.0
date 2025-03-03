package com.mskyeye.handler.service;

import com.alibaba.fastjson.JSON;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.handler.mq.MqConnectionUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.mskyeye.handler.common.GlobalResources.trackHandleQueue;

/**
 * @ClassName:MergeTrackService
 * @Description:主业务类
 * @Author:R.Gong
 * @Date:2023/1/4 18:00
 * @Version:1.0
 **/
@Component
@Slf4j
@RefreshScope
public class MergeAndAlarmHandleService {

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    public void run() throws Exception {

        //解析收到的原始MQ航迹数据
        DefaultConsumer consumer = new DefaultConsumer(mqConnectionUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                //解析成航迹最终格式
                LwTrackPacket twp = JSON.parseObject(msg, LwTrackPacket.class);
                Content cnt = twp.getITEM().get(0);
                //TODO 去除特殊AIS目标
                if (StringUtil.isNotEmpty(cnt.getMMSI()) && cnt.getMMSI() == 1000000000) {
                    return;
                }
                trackHandleQueue.offer(twp);
            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.TRACK_QUEUE_NAME, true, consumer);
    }
}
