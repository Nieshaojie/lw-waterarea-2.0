package com.mskyeye.handler.cron;

import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.model.AisTrackCache;
import com.mskyeye.handler.model.MergeTrackCache;
import com.mskyeye.handler.model.RadarTrackCache;
import com.mskyeye.handler.utils.DataCalUtil;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @ClassName:MergeTask
 * @Description:航迹融合任务
 * @Author:R.Gong
 * @Date:2023/7/13 15:03
 * @Version:1.0
 **/
@Component
public class TrackMergeTask {

    @Value("${merge_dis}")
    private String mergeDis;

    private static final Integer MATCH_THO = 1;//匹配阈值

    @Scheduled(fixedDelay = 2500)
    public void run() throws Exception {
        for (AisTrackCache value : GlobalResources.aisTrackMap.values()) {
            //如果该AIS已被融合,则不再计算
            if(GlobalResources.mergeResultMap.containsKey(value.getTargetId())){
                continue;
            }
            RadarTrackCache radarTrackCache = srcMatchRadarTarByAis(value);//找到匹配的雷达目标
            if(radarTrackCache != null){
                MergeTrackCache mergeTrackCache = GlobalResources.mergeResultMap.get(value.getTargetId());
//                if(mergeTrackCache != null){
//                    //同一雷达目标再次被匹配到
//                    if(mergeTrackCache.getMerAisMmsi() == value.getIMmsi()){
//                        //匹配数加一
//                        if(mergeTrackCache.getMatchNum() < MATCH_THO){
//                            mergeTrackCache.setMatchNum(mergeTrackCache.getMatchNum()+1);
//                        }
//                    }
//                    mergeTrackCache.setRefreshTime(System.currentTimeMillis());//更新时间
//                    //更新融合处理map
//                    GlobalResources.mergeHandleMap.put(mergeTrackCache.getMerAisMmsi(),mergeTrackCache);
//                    //更新融合结果map
//                    if(mergeTrackCache.getMatchNum() >= MATCH_THO){
//                        MergeTrackCache newCacheVal = new MergeTrackCache();
//                        newCacheVal = mergeTrackCache;
//                        GlobalResources.mergeResultMap.put(mergeTrackCache.getMerAisMmsi(),mergeTrackCache);
//                    }
//                }
//                //第一次融合匹配到
//                else{

                if(mergeTrackCache == null){
                    MergeTrackCache newCacheVal = new MergeTrackCache();
                    newCacheVal.setStationId(radarTrackCache.getStationId());//雷达数据的雷达站ID号
                    newCacheVal.setMerAisMmsi(value.getIMmsi());
                    newCacheVal.setMerRadarId(radarTrackCache.getTargetId());
                    newCacheVal.setRefreshTime(System.currentTimeMillis());
                    newCacheVal.setMatchNum(1);
//                    GlobalResources.mergeHandleMap.put(newCacheVal.getMerAisMmsi(),newCacheVal);
                    //TODO 第一次匹配上就融合
                    GlobalResources.mergeResultMap.put(value.getTargetId(),newCacheVal);
                }
            }
        }

        for(LwTrackPacket lwTrackPacket:GlobalResources.beidouTrackMap.values()) {
            Content cnt = lwTrackPacket.getITEM().get(0);
            //如果该北斗已被融合,则不再计算
            if (GlobalResources.mergeResultMap.containsKey(cnt.getTID())) {
                continue;
            }
            RadarTrackCache radarTrackCache = srcMatchRadarTarByBeiDou(lwTrackPacket);//找到匹配的雷达目标
            if (radarTrackCache != null) {
                MergeTrackCache mergeTrackCache = GlobalResources.mergeResultMap.get(cnt.getTID());
                if (mergeTrackCache == null) {
                    MergeTrackCache newCacheVal = new MergeTrackCache();
                    newCacheVal.setStationId(radarTrackCache.getStationId());//雷达数据的雷达站ID号
                    newCacheVal.setMerAisMmsi(cnt.getMMSI());
                    newCacheVal.setMerRadarId(radarTrackCache.getTargetId());
                    newCacheVal.setRefreshTime(System.currentTimeMillis());
                    newCacheVal.setMatchNum(1);
//                    GlobalResources.mergeHandleMap.put(newCacheVal.getMerAisMmsi(),newCacheVal);
                    //TODO 第一次匹配上就融合
                    GlobalResources.mergeResultMap.put(cnt.getTID(), newCacheVal);
                }
            }
        }
    }


    /**
     * 查找匹配的雷达目标(AIS)
     *
     * @param cnt
     * @return
     * @throws Exception
     */
    private RadarTrackCache srcMatchRadarTarByAis(AisTrackCache cnt) throws Exception {
        /*double minDis = Double.parseDouble(mergeDis);//最短距离
        RadarTrackCache matchRadarTrack = null;
        for (RadarTrackCache value : GlobalResources.radarTrackMap.values()) {
            double dis = DataCalUtil.GetDistance(value.getShipLon(), value.getShipLat(),
                    cnt.getShipLon(), cnt.getShipLat());
            if (dis <= minDis) {
                matchRadarTrack = value;
            }
        }
        return matchRadarTrack;*/
        double minDis = Double.parseDouble(mergeDis);
        double best = Double.MAX_VALUE;
        RadarTrackCache matchRadarTrack = null;

        for (RadarTrackCache radar : GlobalResources.radarTrackMap.values()) {
            double dis = DataCalUtil.GetDistance(radar.getShipLon(), radar.getShipLat(),
                    cnt.getShipLon(), cnt.getShipLat());
            if (dis < minDis && dis < best) {
                best = dis;
                matchRadarTrack = radar;
            }
        }
        return matchRadarTrack;

    }

    /**
     * 查找匹配的雷达目标(北斗)
     * @param lwTrackPacket
     * @return
     * @throws Exception
     */
    private RadarTrackCache srcMatchRadarTarByBeiDou(LwTrackPacket lwTrackPacket)throws Exception {
        double minDis = Double.parseDouble(mergeDis);//最短距离
        RadarTrackCache matchRadarTrack = null;
        Content cnt = lwTrackPacket.getITEM().get(0);
        for (RadarTrackCache value : GlobalResources.radarTrackMap.values()) {
            double dis = DataCalUtil.GetDistance(value.getShipLon(), value.getShipLat(),
                    cnt.getLON(), cnt.getLAT());
            if (dis <= minDis) {
                matchRadarTrack = value;
            }
        }
        return matchRadarTrack;
    }
}
