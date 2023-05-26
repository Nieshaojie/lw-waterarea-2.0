package com.mskyeye.ws.mq.handler;

import com.mskyeye.ws.common.GlobalResources;
import com.mskyeye.ws.mq.utils.MqConnectionUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化消息队列配置
        mqConnUtil.initMqConfig();
        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(mqConnUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                //System.out.println(" [消费者1] received : " + msg + "!");
                //如果缓存队列满，出队列
                if(GlobalResources.capTrackQueue.size() > GlobalResources.TRACK_QUEUE_CAP){
                    GlobalResources.capTrackQueue.poll();
                }
                GlobalResources.capTrackQueue.add(msg);
                //如果实时队列满，出队列
                if(GlobalResources.curTrackQueue.size() > GlobalResources.TRACK_QUEUE_CUR){
                    GlobalResources.curTrackQueue.poll();
                }
                GlobalResources.curTrackQueue.add(msg);
            }
        };
        // 监听队列，自动返回完成
        mqConnUtil.getChannel().basicConsume(MqConnectionUtil.QUEUE_NAME, true, consumer);
    }

}
