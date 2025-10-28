package com.mskyeye.handler.service;

import com.google.gson.Gson;
import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.model.*;
import com.mskyeye.handler.mq.MqConnectionUtil;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static com.mskyeye.handler.common.GlobalResources.*;
import static com.mskyeye.handler.mq.MqConnectionUtil.*;

/**
 * @ClassName:TrackHandleService
 * @Description:航迹处理业务
 * @Author:R.Gong
 * @Date:2024/5/23 8:40
 * @Version:1.0
 **/
@Service
public class TrackHandleService {

    @Autowired
    private MqConnectionUtil mqConnectionUtil;

    private static final Integer DEL_TARGET = 0;

    private static final Integer RADAR_TARGET = 0;

    private static final Integer FLY_RADAR_TARGET = 6;

    private static final Integer AIS_TARGET = 1;

    private static final Integer BEIDOU_TARGET = 5;

    private static final Integer AIS_FWD_PRETARGET = 3;//AIS正向外推

    private static final Integer AIS_NEG_PRTARGET = 4;//AIS反向外推

    private static final Integer MERGE_TARGET = 2;

    /**
     * AIS告警
     */
    private static final Integer ALARM_BLACK_LIST = 0;//黑名单
    private static final Integer ALARM_WHITE_LIST = 1;//白名单
    private static final Integer LAW_ENFORECE_SHIP = 2;//执法船

    /**
     * 雷达预警
     */
    private static final Integer IS_PLANE = 1;//是否在面内
    private static final Integer MORE_THRES = 2;//大于阈值
    private static final Integer LESS_THRES = 3;//小于阈值

    /**
     * 雷达预警计算属性
     */
    private static final Integer LONLAT_CAL = 1;//经纬度计算
    private static final Integer SPEED_CAL = 2;//速度计算
    private static final Integer SIZEMETERS_CAL = 3;//回波尺寸计算
    private static final Integer SIZEDEGREES_CAL = 4;//回波展宽计算

    private static final String FLIT_SIGN = "+";

    private static final Integer ALARM_THROS = 5;//雷光警戒连续预警航迹阈值

    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();


//    @Bean
//    public void processDataAsynchronously() {
//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        executor.scheduleAtFixedRate(this::executeTask, 0, 1, TimeUnit.SECONDS);
//    }

    @Bean
    private void executeTask() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (!trackHandleQueue.isEmpty()) {
                        LwTrackPacket twp = trackHandleQueue.poll();
                        Content cnt = twp.getITEM().get(0);
                        //如果是删除目标数据包，直接给预警处理模块
                        if (cnt.getSTATUS().equals(DEL_TARGET)) {
                            //历史航迹点Map需要及时删除该预警目标
                            oldTrackMap.remove(cnt.getTID());
                            mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                    properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                    properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                    properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                        }
                        //雷达目标处理
                        if (cnt.getSOURCE() == RADAR_TARGET) {
                            RadarTrackCache radarTrackCache = parseToRadarTar(cnt, twp.getTIME());//解析成雷达目标
                            if (radarTrackMap.containsKey(radarTrackCache.getTargetId())) {
                                Integer refNum = radarTrackMap.get(radarTrackCache.getTargetId()).getRefNum();
                                if (refNum < ALARM_THROS) {
                                    refNum = refNum + 1;
                                }
                                radarTrackCache.setRefNum(refNum);
                            }
                            radarTrackMap.put(radarTrackCache.getTargetId(), radarTrackCache);//插入或更新雷达缓存map
                            for (Map.Entry<Long, MergeTrackCache> entry : GlobalResources.mergeResultMap.entrySet()) {
                                if (cnt.getTID() == entry.getValue().getMerRadarId().longValue()) {
                                    Long mmsi = entry.getKey();
                                    //融合目标加上AIS静态数据
                                    cnt = insertAisStaticInfo(cnt, mmsi);
                                    cnt.setTID(mmsi.longValue());//融合航迹的批号要换成mmsi
                                    cnt.setMMSI(mmsi);
                                    //标志位更改为融合目标
                                    cnt.setSOURCE(MERGE_TARGET);
                                    break;
                                }
                            }
                            //预警处理
                            //TODO
                            twp = alarmHandler(twp);
                            if (StringUtil.isNotEmpty(twp)) {
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        //反无雷达目标处理
                        if (cnt.getSOURCE() == FLY_RADAR_TARGET) {
                            RadarTrackCache radarTrackCache = parseToFlyRadarTar(cnt, twp.getTIME());//解析成雷达目标
                            if (radarTrackMap.containsKey(radarTrackCache.getTargetId())) {
                                Integer refNum = radarTrackMap.get(radarTrackCache.getTargetId()).getRefNum();
                                if (refNum < ALARM_THROS) {
                                    refNum = refNum + 1;
                                }
                                radarTrackCache.setRefNum(refNum);
                            }
                            radarTrackMap.put(radarTrackCache.getTargetId(), radarTrackCache);//插入或更新雷达缓存map
                            for (Map.Entry<Long, MergeTrackCache> entry : GlobalResources.mergeResultMap.entrySet()) {
                                if (cnt.getTID() == entry.getValue().getMerRadarId().longValue()) {
                                    Long mmsi = entry.getKey();
                                    //融合目标加上AIS静态数据
                                    cnt = insertAisStaticInfo(cnt, mmsi);
//                                    cnt.setTID(mmsi.longValue());//融合航迹的批号要换成mmsi
                                    cnt.setMMSI(mmsi);
                                    //标志位更改为融合目标
                                    cnt.setSOURCE(MERGE_TARGET);
                                    break;
                                }
                            }
                            //预警处理
                            //TODO
                            twp = alarmHandler(twp);
                            if (StringUtil.isNotEmpty(twp)) {
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        //北斗目标处理
                        else if(cnt.getSOURCE() == BEIDOU_TARGET){
                            //如果该北斗目标被融合,则不显示北斗目标，只显示雷达与北斗的融合目标
                            if (GlobalResources.mergeResultMap.containsKey(cnt.getTID())) {
                                continue;
                            }
                            if (mqConnectionUtil.getChannel() != null && StringUtil.isNotEmpty(twp)) {
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        //AIS真实目标处理
                        else if (cnt.getSOURCE() == AIS_TARGET) {
                            AisTrackCache aisTrackCache = parseToAisTar(cnt, twp.getTIME());//解析成AIS目标
                            GlobalResources.aisTrackMap.put(aisTrackCache.getTargetId(), aisTrackCache);//插入或更新AIS缓存map
                            //如果不是融合目标,给告警处理模块。否则,舍弃该AIS数据
                            if (GlobalResources.mergeResultMap.containsKey(aisTrackCache.getIMmsi())) {
                                continue;
                            }
                            //加上AIS静态数据
                            if (aisTrackCache.getIMmsi() != -1) {
                                cnt = insertAisStaticInfo(cnt, aisTrackCache.getIMmsi());
                            }
                            //告警处理
                            //TODO
                            twp = alarmHandler(twp);
                            if (mqConnectionUtil.getChannel() != null && StringUtil.isNotEmpty(twp)) {
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        //AIS外推目标处理
                        else if (cnt.getSOURCE() == AIS_FWD_PRETARGET || cnt.getSOURCE() == AIS_NEG_PRTARGET) {
                            AisTrackCache aisTrackCache = parseToAisTar(cnt, twp.getTIME());//解析成AIS目标
                            GlobalResources.aisTrackMap.put(aisTrackCache.getTargetId(), aisTrackCache);//插入或更新AIS缓存map
                            //如果不是融合目标,给告警处理模块。否则,舍弃该AIS外推数据
                            if (GlobalResources.mergeResultMap.containsKey(aisTrackCache.getIMmsi())) {
                                continue;
                            }
                            //加上AIS静态数据
                            cnt = insertAisStaticInfo(cnt, aisTrackCache.getIMmsi());
                            //TODO
                            cnt.setSOURCE(AIS_TARGET);
                            //告警处理
                            //TODO
                            twp = alarmHandler(twp);
                            if (StringUtil.isNotEmpty(twp)) {
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY1,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY2,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                                mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, PRO_TRACK_QUEUE_ROUTING_KEY3,
                                        properties, new Gson().toJson(twp).getBytes(StandardCharsets.UTF_8));
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * 航迹数据包解析成雷达航迹缓存对象
     *
     * @param cnt
     * @return
     * @throws Exception
     */
    private RadarTrackCache parseToRadarTar(Content cnt, Long time) throws Exception {
        RadarTrackCache radarTrackCache = new RadarTrackCache();

        radarTrackCache.setStationId(cnt.getSTATIONID());
        radarTrackCache.setTargetId(cnt.getTID());
        radarTrackCache.setShipLat(cnt.getLAT());
        radarTrackCache.setShipLon(cnt.getLON());
        radarTrackCache.setRefreshTime(time);

        return radarTrackCache;
    }

    /**
     * 航迹数据包解析成雷达航迹缓存对象
     *
     * @param cnt
     * @return
     * @throws Exception
     */
    private RadarTrackCache parseToFlyRadarTar(Content cnt, Long time) throws Exception {
        RadarTrackCache radarTrackCache = new RadarTrackCache();

        radarTrackCache.setStationId(cnt.getSTATIONID());
        radarTrackCache.setTargetId(cnt.getTID());
        radarTrackCache.setShipLat(cnt.getLAT());
        radarTrackCache.setShipLon(cnt.getLON());
        radarTrackCache.setAlt(cnt.getALT());
        radarTrackCache.setRefreshTime(time);

        return radarTrackCache;
    }

    /**
     * 航迹数据包解析成AIS航迹缓存对象
     *
     * @param cnt
     * @return
     * @throws Exception
     */
    private AisTrackCache parseToAisTar(Content cnt, Long time) throws Exception {
        AisTrackCache aisTrackCache = new AisTrackCache();

        aisTrackCache.setStationId(cnt.getSTATIONID());
        aisTrackCache.setTargetId(cnt.getTID());
        aisTrackCache.setIMmsi(cnt.getMMSI());
        aisTrackCache.setShipLat(cnt.getLAT());
        aisTrackCache.setShipLon(cnt.getLON());
        aisTrackCache.setAlt(cnt.getALT());
        aisTrackCache.setRefreshTime(time);

        return aisTrackCache;
    }

    /**
     * 航迹信息插入AIS静态数据
     *
     * @param cnt
     * @return
     * @throws Exception
     */
    private Content insertAisStaticInfo(Content cnt, Long mmsi) throws Exception {
        if (GlobalResources.aisStaticInfoMap != null
                && !(GlobalResources.aisStaticInfoMap.isEmpty())
                && GlobalResources.aisStaticInfoMap.containsKey(mmsi.longValue())) {
            YzAisStaticInfo yzAisStaticInfo = GlobalResources.aisStaticInfoMap.get(mmsi.longValue());
            if (yzAisStaticInfo.getShipName() != null) {
                cnt.setSHIPNAME(yzAisStaticInfo.getShipName());
            }
            if (yzAisStaticInfo.getDraught() != null) {
                cnt.setDRAUGHT(yzAisStaticInfo.getDraught());
            }
            if (yzAisStaticInfo.getImo() != null) {
                cnt.setIMO(yzAisStaticInfo.getImo());
            }
            if (yzAisStaticInfo.getCallSign() != null) {
                cnt.setCALLSIGN(yzAisStaticInfo.getCallSign());
            }
            if (yzAisStaticInfo.getShipType() != null) {
                cnt.setSHIPTYPE(yzAisStaticInfo.getShipType());
            }
            if (yzAisStaticInfo.getCountry() != null) {
                cnt.setCOUNTRY(yzAisStaticInfo.getCountry());
            }
            if (yzAisStaticInfo.getShipLength() != null) {
                cnt.setSHIPLENGTH(yzAisStaticInfo.getShipLength());
            }
            if (yzAisStaticInfo.getShipWidth() != null) {
                cnt.setSHIPWIDTH(yzAisStaticInfo.getShipWidth());
            }
        }
        return cnt;
    }


    /**
     * 告警处理
     *
     * @param lwTrackPacket
     * @return
     */
    private LwTrackPacket alarmHandler(LwTrackPacket lwTrackPacket) throws IOException {
        Content cnt = lwTrackPacket.getITEM().get(0);
        //AIS目标处理、融合目标
        if (cnt.getSOURCE() == 1 || cnt.getSOURCE() == 2
                || cnt.getSOURCE() == 3 || cnt.getSOURCE() == 4) {
            if (!GlobalResources.aisAlarmCalInfoMap.isEmpty() && GlobalResources.aisAlarmCalInfoMap.containsKey(cnt.getMMSI().intValue())) {
                //白名单处理
                if (ALARM_WHITE_LIST == GlobalResources.aisAlarmCalInfoMap.get(cnt.getMMSI().intValue()).getAlarmType()) {
                    return lwTrackPacket;
                }
                //黑名单处理
                else if (ALARM_BLACK_LIST == GlobalResources.aisAlarmCalInfoMap.get(cnt.getMMSI().intValue()).getAlarmType()) {
                    cnt.setALARM("黑名单");
                }
                //执法船处理
                else if (LAW_ENFORECE_SHIP == GlobalResources.aisAlarmCalInfoMap.get(cnt.getMMSI().intValue()).getAlarmType()) {
                    cnt.setALARM("执法船");
                }
            }
        }
        //雷达目标
        else if (cnt.getSOURCE() == 0 || cnt.getSOURCE() == 6) {
            //只有连续多少帧以上才判断为稳定目标,才有预警价值
            if(!radarTrackMap.containsKey(cnt.getTID()) || radarTrackMap.get(cnt.getTID()).getRefNum() < ALARM_THROS){
                cnt.setALARM("");
                return lwTrackPacket;
            }
            if (!GlobalResources.code2DeptIdMap.isEmpty() && GlobalResources.code2DeptIdMap.containsKey(cnt.getSTATIONID().intValue())) {
                Integer deptId = GlobalResources.code2DeptIdMap.get(cnt.getSTATIONID().intValue());
                List<RadarAlarmCalInfo> list = GlobalResources.radarAlarmCalInfoMap.get(deptId.intValue());
                String alarmInfo = "";
                if (list != null && !list.isEmpty()) {
                    for (RadarAlarmCalInfo tmpInfo : list) {
                        //判断是否在预警区域内
                        boolean isInPolygon = isPointInPolygon(cnt.getLON(), cnt.getLAT(), tmpInfo.getPointsLonLat());
                        if (isInPolygon) {
                            Integer calMethod = tmpInfo.getCalMethod();
                            Integer calPro = tmpInfo.getCalPro();
                            Integer calVal = tmpInfo.getTypeValue();
                            Integer isInterAlarm = tmpInfo.getIsInterAlarm();
                            LocalTime startTime = LocalTime.parse(tmpInfo.getStartTime());
                            LocalTime endTime = LocalTime.parse(tmpInfo.getEndTime());
                            LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Shanghai"));
                            //如果当前时间不在该预警区域的起止使能时间内,则不用预警输出
                            if (isInterAlarm == 1) {
                                if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                                    continue;
                                }
                            }
                            if (isInterAlarm == 0) {
                                if (currentTime.isBefore(endTime) && currentTime.isAfter(startTime)) {
                                    continue;
                                }
                            }
                            //是否在面内
                            if (calMethod == IS_PLANE) {
                                alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                            }
                            //大于阈值
                            else if (calMethod == MORE_THRES) {
                                if (calPro == SPEED_CAL) {
                                    //满足速度类的预警
                                    if (cnt.getSPEED() > calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                } else if (calPro == SIZEMETERS_CAL) {
                                    //满足回波尺寸类的预警
                                    if (cnt.getSIZEMETRES() > calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                } else if (calPro == SIZEDEGREES_CAL) {
                                    //满足回波展宽类的预警
                                    if (cnt.getSIZEDEGREES() > calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                }
                            }
                            //小于阈值
                            else if (calMethod == LESS_THRES) {
                                if (calPro == SPEED_CAL) {
                                    //满足速度类的预警
                                    if (cnt.getSPEED() < calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                } else if (calPro == SIZEMETERS_CAL) {
                                    //满足回波尺寸类的预警
                                    if (cnt.getSIZEMETRES() < calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                } else if (calPro == SIZEDEGREES_CAL) {
                                    //满足回波展宽类的预警
                                    if (cnt.getSIZEDEGREES() < calVal) {
                                        alarmInfo += FLIT_SIGN + tmpInfo.getTypeName();
                                    }
                                }
                            }
                        }
                    }
                }
                if (StringUtil.isNotEmpty(alarmInfo)) {
                    alarmInfo = alarmInfo.replaceFirst("\\+", "");
                }
                cnt.setALARM(alarmInfo);
                //有该预警目标,更新信息
                 /*if (oldTrackMap.containsKey(cnt.getTID())) {
                    oldTrackMap.put(cnt.getTID(), lwTrackPacket);
                } else*/ if (!alarmInfo.equals("") /*&& !oldTrackMap.containsKey(cnt.getTID())*/) {
                    mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, "new_alarm.key",
                            properties, new Gson().toJson(lwTrackPacket).getBytes(StandardCharsets.UTF_8));
                    oldTrackMap.put(cnt.getTID(), lwTrackPacket);
//                    System.out.println("【航迹服务】新增报警,ID为" + cnt.getTID() + " 报警类型为【" + alarmInfo + "】");
                }
            }
        }
        return lwTrackPacket;
    }

    /**
     * 判断点是否在面内
     *
     * @param lon
     * @param lat
     * @param points
     * @return
     */
    private boolean isPointInPolygon(double lon, double lat, List<LonLatInfo> points) {
        boolean inside = false;
        for (int i = 0; i < points.size(); i++) {
            LonLatInfo current = points.get(i);
            LonLatInfo next = points.get((i + 1) % points.size());
            if (((current.getLat() <= lat) && (lat < next.getLat())) || ((next.getLat() <= lat) && (lat < current.getLat()))) {
                double x = (next.getLon() - current.getLon()) * (lat - current.getLat()) / (next.getLat() - current.getLat()) + current.getLon();
                if (x < lon) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }
}
