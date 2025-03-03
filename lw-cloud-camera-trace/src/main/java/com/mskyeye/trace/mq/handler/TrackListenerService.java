package com.mskyeye.trace.mq.handler;


import com.alibaba.fastjson.JSON;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.mq.utils.MqConnectionUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.mskyeye.trace.common.GlResources.GL_RCAlarmMap;
import static com.mskyeye.trace.common.GlResources.GL_TraceInfoMap;

/**
 * @ClassName:TrackListenerService
 * @Description:航迹监听服务，用于跟踪
 * @Author:R.Gong
 * @Date:2023/8/20 17:20
 * @Version:1.0
 **/
@Component
@RefreshScope
@Slf4j
public class TrackListenerService {

    @Autowired
    private MqConnectionUtil mqConnectionUtil;


    public void run() throws Exception {

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(mqConnectionUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                //解析成航迹数据对象
                LwTrackPacket lwTrackPacket = JSON.parseObject(new String(body, CharsetUtil.UTF_8), LwTrackPacket.class);
                Content cnt = lwTrackPacket.getITEM().get(0);
                //反向外推AIS数据不用监听
                if(cnt.getSOURCE() == 4){
                    return;
                }
                for(Map.Entry<Long, TraceProInfo> entry:GL_TraceInfoMap.entrySet()){
                    TraceProInfo traceProInfo = entry.getValue();
                    //进行跟踪目标经纬度更新，排除AI巡航
                    if(traceProInfo.getTraceType() != 5 && traceProInfo.getTargetId().equals(cnt.getTID())){

//                        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());

                        traceProInfo.setTraceLon(cnt.getLON());
                        traceProInfo.setTraceLat(cnt.getLAT());
                        traceProInfo.setOrderTime(System.currentTimeMillis());//这里用作更新时间
                        entry.setValue(traceProInfo);
                    }
                }
            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.TRACK_QUEUE_NAME, true, consumer);



        DefaultConsumer consumer1 = new DefaultConsumer(mqConnectionUtil.getChannel()) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @SneakyThrows
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                //解析成航迹数据对象
                LwTrackPacket lwTrackPacket = JSON.parseObject(new String(body, CharsetUtil.UTF_8), LwTrackPacket.class);
                Content cnt = lwTrackPacket.getITEM().get(0);
//                System.out.println("【相机服务】新收到报警,ID为" + cnt.getTID());
                GL_RCAlarmMap.put(cnt.getTID(),lwTrackPacket);
            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.NEW_ALARM_QUEUE_NAME, true, consumer1);
    }
}
