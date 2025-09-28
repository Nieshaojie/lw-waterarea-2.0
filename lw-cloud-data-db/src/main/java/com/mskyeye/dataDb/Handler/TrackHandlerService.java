package com.mskyeye.dataDb.Handler;


import com.alibaba.fastjson.JSON;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.dataDb.config.InfluxDBConfig;
import com.mskyeye.dataDb.model.TrackInfo;
import com.mskyeye.dataDb.utils.MqConnectionUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
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
import java.time.LocalDateTime;

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
    private InfluxDBConfig influxDBConfig;

    private Integer count = 0;//用于抽取存储计数

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
                //抽取操作
                if(count < 10){
                    ++count;
                    return;
                }else{
                    count = 0;
                }
                //外推AIS数据不用存
                if(cnt.getSOURCE() == 3 || cnt.getSOURCE() == 4){
                    return;
                }
                TrackInfo trackInfo = parseToTrackInfo(cnt);
                //存入influxDB
                if(StringUtil.isNotEmpty(trackInfo)){
                    //先判断是否要创建数据表
                    influxDBConfig.createTableIfNotExists(influxDBConfig.getUrl(),influxDBConfig.getTodayTableName());
                    //存入航迹
                    influxDBConfig.storeTrackData(influxDBConfig.getTodayTableName(), LocalDateTime.now(), trackInfo);
//                    influxDBConfig.influxDB.close();
                }
            }
        };
        // 监听队列
        mqConnectionUtil.getChannel().basicConsume(mqConnectionUtil.TRACK_QUEUE_NAME, true, consumer);
    }

    /**
     * 实时航迹信息解析成存储的航迹
     * @param cnt
     * @return
     * @throws Exception
     */
    private TrackInfo parseToTrackInfo(Content cnt)throws Exception{
        if(StringUtil.isNotEmpty(cnt)){
            TrackInfo trackInfo = new TrackInfo();

            trackInfo.setId(cnt.getTID());
            trackInfo.setLat(cnt.getLAT());
            trackInfo.setHead(cnt.getHEAD());
            trackInfo.setCountry(cnt.getCOUNTRY());
            trackInfo.setCourse(cnt.getCOURSE());
            trackInfo.setAlarm(cnt.getALARM());
            trackInfo.setLon(cnt.getLON());
            trackInfo.setMmsi(cnt.getMMSI());
            trackInfo.setSpeed(cnt.getSPEED());
            trackInfo.setShipType(cnt.getSHIPTYPE());
            trackInfo.setStationId(cnt.getSTATIONID());
            trackInfo.setStatus(cnt.getSTATUS());
            trackInfo.setName(cnt.getSHIPNAME());
            trackInfo.setSource(cnt.getSOURCE());
            return trackInfo;
        }
        return null;
    }
}
