package com.mskyeye.dataDb.Handler;


import com.mskyeye.dataDb.utils.EsTools;
import com.mskyeye.dataDb.utils.MqConnectionUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName:TrackHandlerService
 * @Description:航迹处理服务
 * @Author:R.Gong
 * @Date:2022/11/21 17:20
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
public class TrackHandlerService{

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    @Autowired
    private EsTools esTools;


    public void run() throws Exception {
        //初始化消息队列配置
//        mqConnectionUtil.initMqConfig();

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(mqConnectionUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                //解析成航迹数据对象
//                LwTrackPacket lwTrackPacket = JSON.parseObject(new String(body, CharsetUtil.UTF_8), LwTrackPacket.class);
//                LwTrackPacket.Content cnt = JSON.parseObject(String.valueOf(lwTrackPacket.getITEM().get(0)), LwTrackPacket.Content.class);
//                String content = "{\n" +
//                        "    \"target_id\":" + cnt.getTID() + ",\n" +
//                        "    \"target_type\":" + cnt.getSOURCE() + ",\n" +
//                        "    \"track_time\":" + lwTrackPacket.getTIME() + ",\n" +
//                        "    \"content\":" + new Gson().toJson(lwTrackPacket) + "\n" +
//                        "}";

                //向ES中存储航迹数据
//                IndexResponse indexResponse = esTools.insertJson("track_" + DateUtil.getCurDataForStorage(),content);

            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.TRACK_QUEUE_NAME, true, consumer);
    }
}
