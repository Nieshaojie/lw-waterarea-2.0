package com.mskyeye.trace.detect;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.mskyeye.trace.common.GlResources;
import com.mskyeye.trace.model.*;
import com.mskyeye.trace.mq.utils.MqConnectionUtil;
import com.mskyeye.trace.service.IYzAlarmEventService;
import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static com.mskyeye.trace.common.GlResources.*;

/**
 * @ClassName:MessageReceiver
 * @Description:识别检测消息接收器
 * @Author:R.Gong
 * @Date:2023/8/29 11:58
 * @Version:1.0
 **/
@Service
@RefreshScope
public class MessageReceiver implements MessageListener {

    @Value("${alarm_base_url}")
    private String alarmBaseUrl;

    @Autowired
    private IYzAlarmEventService iYzAlarmEventService;

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    // 设置消息的TTL为10秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 消息体
        String body = new String(message.getBody());
        DeviceMonitorInfo result = JSONObject.parseObject(body, DeviceMonitorInfo.class);
        GlResources.Gl_MonitorInfo = result;

        DeviceMonitorInfoBody obj = result.getObject();
        String[] tmpArray = obj.getCameraId().split("-");
        String ip = tmpArray[0];
        YzCameraInfo yzCameraInfo = null;
        for(Map.Entry<Long, YzCameraInfo> entry:GL_CameraInfoMap.entrySet()){
            if(entry.getValue().getIp().equals(ip)){
                yzCameraInfo = entry.getValue();
            }
        }
        if(yzCameraInfo == null){
            return;
        }
        YzAiPointInfo yzAiPointInfo;
        YzAlarmEvent yzAlarmEvent = new YzAlarmEvent();
        yzAlarmEvent.setAlarmTime(new Date());
        yzAlarmEvent.setCameraName(yzCameraInfo.getName());

        LwAiAlarmPacket lwAiAlarmPacket = new LwAiAlarmPacket();
        //捕鱼识别
        if(obj.getType() == 68){
            yzAiPointInfo = GL_CurPointInfoMap.get(yzCameraInfo.getId());
            if(yzAiPointInfo == null){
                return;
            }
            yzAlarmEvent.setEventType(1L);//垂钓
            yzAlarmEvent.setLat(yzAiPointInfo!=null?yzAiPointInfo.getLat().doubleValue():obj.getLat());
            yzAlarmEvent.setLon(yzAiPointInfo!=null?yzAiPointInfo.getLon().doubleValue():obj.getLng());
            yzAlarmEvent.setAiPointName(yzAiPointInfo.getName());

            lwAiAlarmPacket.setLAT(yzAiPointInfo.getLat().doubleValue());
            lwAiAlarmPacket.setLON(yzAiPointInfo.getLon().doubleValue());
            lwAiAlarmPacket.setAIPOINTNAME(yzAiPointInfo.getName());
        }
        //船只识别
        else if(obj.getType() == 71){
            yzAlarmEvent.setEventType(4L);//船只
            yzAlarmEvent.setAiPointName("");
            lwAiAlarmPacket.setAIPOINTNAME("");
            TraceProInfo traceProInfo = GL_TraceInfoMap.get(yzCameraInfo.getId());
            if(traceProInfo != null){
                yzAlarmEvent.setLat(traceProInfo.getTraceLat());
                yzAlarmEvent.setLon(traceProInfo.getTraceLon());
                lwAiAlarmPacket.setLAT(traceProInfo.getTraceLat());
                lwAiAlarmPacket.setLON(traceProInfo.getTraceLon());
            }
        }
        yzAlarmEvent.setCameraId(yzCameraInfo.getId());
        yzAlarmEvent.setDeptId(yzCameraInfo.getDeptId());
        String[] imageUrl = obj.getImageUrl().split("//");
        imageUrl[1] = alarmBaseUrl;
        yzAlarmEvent.setPhotoUrl1(imageUrl[0] + "//" + imageUrl[1] + "//" + imageUrl[2]);
        String[] videoUrl = obj.getVideoUrl().split("//");
        videoUrl[1] = alarmBaseUrl;
        yzAlarmEvent.setVideoUrl(videoUrl[0] + "//" +videoUrl[1] + "//" +videoUrl[2]);
        iYzAlarmEventService.insertYzAlarmEvent(yzAlarmEvent);//告警事件插入数据库


        lwAiAlarmPacket.setCMDTYPE("AIALARM");
        lwAiAlarmPacket.setDEPTID(yzCameraInfo.getDeptId());
        lwAiAlarmPacket.setCAMERAID(yzCameraInfo.getId());
        lwAiAlarmPacket.setCAMERANAME(yzCameraInfo.getName());
        lwAiAlarmPacket.setEVENTTYPE(yzAlarmEvent.getEventType());
        //发送给RabbitMQ,复用相机状态队列
        try {
            mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, "ai_alarm.key",
                    properties, new Gson().toJson(lwAiAlarmPacket).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
