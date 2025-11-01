package com.mskyeye.handler.cron;


import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.model.AisTrackCache;
import com.mskyeye.handler.model.MergeTrackCache;
import com.mskyeye.handler.model.RadarTrackCache;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName:TraversalListTask
 * @Description:清除过期目标任务
 * @Author:R.Gong
 * @Date:2022/11/26 14:30
 * @Version:1.0
 **/
@Component
public class TraversalListTask {

    @Scheduled(fixedDelay = 3000)
    public void run() {

        Iterator<Map.Entry<Long, RadarTrackCache>> iter = GlobalResources.radarTrackMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,RadarTrackCache> entry = iter.next();
            if (System.currentTimeMillis() - entry.getValue().getRefreshTime() > 5000) {
                iter.remove();
            }
        }

        Iterator<Map.Entry<Long, AisTrackCache>> iter1 = GlobalResources.aisTrackMap.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry<Long,AisTrackCache> entry = iter1.next();
            if (System.currentTimeMillis() - entry.getValue().getRefreshTime() > 5000) {
                iter1.remove();
            }
        }

        Iterator<Map.Entry<Long, LwTrackPacket>> iter2 = GlobalResources.beidouTrackMap.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry<Long,LwTrackPacket> entry = iter2.next();
            if (System.currentTimeMillis() - entry.getValue().getTIME() > 300000) {
                iter2.remove();
            }
        }

//        Iterator<Map.Entry<Integer, MergeTrackCache>> iter2 = GlobalResources.mergeHandleMap.entrySet().iterator();
//        while (iter2.hasNext()) {
//            Map.Entry<Integer,MergeTrackCache> entry = iter2.next();
//            if (System.currentTimeMillis() - entry.getValue().getRefreshTime() > 30000) {
//                iter2.remove();
//            }
//        }

        Iterator<Map.Entry<Long, MergeTrackCache>> iter3 = GlobalResources.mergeResultMap.entrySet().iterator();
        while (iter3.hasNext()) {
            Map.Entry<Long,MergeTrackCache> entry = iter3.next();
            //如果融合结果map中的雷达数据删批,则删除该融合结果
            if (!GlobalResources.radarTrackMap.containsKey(entry.getValue().getMerRadarId().longValue())) {
                iter3.remove();
            }
        }

        Iterator<Map.Entry<Long, LwTrackPacket>> iter4 = GlobalResources.oldTrackMap.entrySet().iterator();
        while (iter4.hasNext()) {
            Map.Entry<Long,LwTrackPacket> entry = iter4.next();
            if (System.currentTimeMillis() - entry.getValue().getTIME() > 600000) {
                iter4.remove();
            }
        }
    }
}
