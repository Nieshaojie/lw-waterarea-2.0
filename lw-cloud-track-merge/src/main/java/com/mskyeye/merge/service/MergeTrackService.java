package com.mskyeye.merge.service;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.LwTrackPacket;
import com.mskyeye.merge.common.GlobalResources;
import com.mskyeye.merge.model.AisTrackCache;
import com.mskyeye.merge.model.RadarTrackCache;
import com.mskyeye.merge.mq.MqConnectionUtil;
import com.mskyeye.merge.utils.DataCalUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName:MergeTrackService
 * @Description:主业务类
 * 如果是AIS数据，遍历AIS队列，如果队列中没有该AIS数据，加入该AIS数据（融合标志位为false、融合匹配帧数至0）；
 * 如果该AIS已被融合，则丢弃该条数据；如果队列中有该AIS数据且融合匹配帧数小于5，遍历雷达数据，尝试查找待融合的雷达目标（距离小于远程配置的阈值参数）；
 * 如果融合匹配帧数大于5，该雷达数据添加AIS信息并打上融合标记且该AIS的融合标志位为true并丢弃该条数据。
 * 如果是雷达数据，查找雷达队列的融合信息，整理后发到融合航迹RabbitMQ中。
 * 如果数据未被丢弃，添加至融合航迹RabbitMQ中。
 * AIS数据每次操作都要修改更新时间，定时任务30s遍历AIS队列，如果某AIS超过3分钟未更新信息，雷达队列删除融合的AIS信息及标记，AIS队列中的融合标志位至为false、融合计算帧数至0。
 * @Author:R.Gong
 * @Date:2023/1/4 18:00
 * @Version:1.0
 **/
@Component
@Slf4j
@RefreshScope
public class MergeTrackService implements ApplicationRunner {

    @Autowired
    private MqConnectionUtil mqConnUtil;

    @Value("${merge_dis}")
    private String mergeDis;

    private static final Integer INSERT_TARGET = 1;

    private static final Integer RADAR_TARGET = 0;

    private static final Integer AIS_TARGET = 1;

    private static final Integer MERGE_TARGET = 2;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("服务端启动中");
        //连接RabbitMQ
        mqConnUtil.initMqConfig();
        try {
            //解析收到的原始MQ航迹数据
            DefaultConsumer consumer = new DefaultConsumer(mqConnUtil.getOriginalTrackChannel()) {
                // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
                @SneakyThrows
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    // body 即消息体
                    String msg = new String(body);
                    //解析成航迹最终格式
                    LwTrackPacket twp = JSON.parseObject(msg, LwTrackPacket.class);
                    LwTrackPacket.Content cnt = JSON.parseObject(String.valueOf(twp.getITEM().get(0)), LwTrackPacket.Content.class);
                    Boolean bEnMQ = true;//是否入融合MQ
                    //如果不是新增目标，直接发给MQ
                    if(cnt.getSOURCE().equals(INSERT_TARGET)){
                        //AIS目标处理
                        if(cnt.getSOURCE().equals(AIS_TARGET)){
                            CopyOnWriteArrayList<AisTrackCache> atc = GlobalResources.aisTrackList;
                            int i = 0;
                            for (;i < atc.size();++i){
                                AisTrackCache aisTrackTmp = atc.get(i);
                                //如果AIS缓存链表中有该AIS数据
                                if(aisTrackTmp.getIMmsi() == cnt.getMMSI()){
                                    //如果该AIS已被融合，则丢弃该条数据,不入MQ
                                    if(aisTrackTmp.getBMergeTar()){
                                        bEnMQ = false;
                                        break;
                                    }
                                    //如果队列中有该AIS数据且融合匹配帧数小于5，遍历雷达数据，
                                    //尝试查找待融合的雷达目标（距离小于远程配置的阈值参数）
                                    //如果匹配的雷达目标是同一目标，融合帧数加一，否则重置
                                    if(aisTrackTmp.getMatchFrames() < 5){
                                        RadarTrackCache matchRadarInfo = srcMatchRadarTar(cnt);
                                        if (matchRadarInfo == null){
                                            break;
                                        }
                                        //如果没有匹配信息，新增
                                        if(aisTrackTmp.getStationId() == null || aisTrackTmp.getRadarTarId() == null){
                                            aisTrackTmp.setStationId(matchRadarInfo.getStationId());
                                            aisTrackTmp.setRadarTarId(matchRadarInfo.getTargetId());
                                        }
                                        if(aisTrackTmp.getStationId() == matchRadarInfo.getStationId() &&
                                                aisTrackTmp.getRadarTarId() == matchRadarInfo.getTargetId()){
                                            aisTrackTmp.setMatchFrames(aisTrackTmp.getMatchFrames() + 1);
                                            atc.set(i,aisTrackTmp);
                                        }
                                        //如果融合匹配帧数大于等于5，该雷达数据添加AIS信息并打上融合标记且该AIS的融合标志位为true并丢弃该条数据
                                        if(aisTrackTmp.getMatchFrames() >= 5){
                                            matchRadarInfo.setBMergeTar(true);
                                            matchRadarInfo.setIMmsi(aisTrackTmp.getIMmsi());
                                            bEnMQ = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            //如果缓存中没有该AIS目标，进行添加操作
                            if (i == atc.size()){
                                AisTrackCache aisTrackCache = new AisTrackCache();
                                aisTrackCache.setTargetId(cnt.getTID());
                                aisTrackCache.setIMmsi(cnt.getMMSI());
                                aisTrackCache.setRefreshTime(twp.getTIME());
                                GlobalResources.aisTrackList.add(aisTrackCache);
                            }
                        }

                        //雷达目标处理
                        else if(cnt.getSOURCE().equals(RADAR_TARGET)){
                            int j = 0;
                            for(;j < GlobalResources.radarTrackList.size();++j){
                                //如果缓存中有该雷达目标，更新匹配的AIS信息后发MQ
                                if(GlobalResources.radarTrackList.get(j).getStationId() == cnt.getSTATIONID() &&
                                        GlobalResources.radarTrackList.get(j).getTargetId() == cnt.getTID()){
                                    cnt.setSOURCE(MERGE_TARGET);
                                    cnt.setNAME(GlobalResources.radarTrackList.get(j).getShipName());
                                    cnt.setMMSI(GlobalResources.radarTrackList.get(j).getIMmsi());
                                    List<String> list = null;
                                    list.add(new Gson().toJson(cnt));
                                    twp.setITEM(list);
                                }
                            }
                            //如果缓存中没有该雷达目标，新增该雷达目标缓存
                            if(j == GlobalResources.radarTrackList.size()){
                                RadarTrackCache radarTrackCache = new RadarTrackCache();
                                radarTrackCache.setStationId(cnt.getSTATIONID());
                                radarTrackCache.setTargetId(cnt.getTID());
                                radarTrackCache.setShipLon(cnt.getLON());
                                radarTrackCache.setShipLat(cnt.getLAT());
                                radarTrackCache.setRefreshTime(twp.getTIME());
                                GlobalResources.radarTrackList.add(radarTrackCache);
                            }
                        }
                    }
                    //入融合MQ操作
                    if(bEnMQ){
                        mqConnUtil.getMergeTrackChannel().basicPublish(MqConnectionUtil.MERGE_EXCHANGE_NAME,"",
                                null, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【融合服务】【异常信息】" + e.getMessage());
        }
    }

    /**
     * 查找匹配的雷达目标
     * @param cnt
     * @return
     * @throws Exception
     */
    private RadarTrackCache srcMatchRadarTar(LwTrackPacket.Content cnt)throws Exception{
        double minDis = Double.parseDouble(mergeDis);//最短距离
        RadarTrackCache matchRadarTrack = null;
        for(RadarTrackCache radarTrackCache:GlobalResources.radarTrackList){
            double dis = DataCalUtil.GetDistance(radarTrackCache.getShipLon(),radarTrackCache.getShipLat(),
                    cnt.getLON(),cnt.getLAT());
            if(dis <= minDis){
                matchRadarTrack = radarTrackCache;
            }
        }
        return matchRadarTrack;
    }


}
