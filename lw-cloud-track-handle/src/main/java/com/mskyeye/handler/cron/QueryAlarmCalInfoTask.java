package com.mskyeye.handler.cron;

import com.mskyeye.common.utils.StringUtil;
import com.mskyeye.handler.common.GlobalResources;
import com.mskyeye.handler.mapper.YzAisAlarmConfigMapper;
import com.mskyeye.handler.mapper.YzRadarAlarmCalInfoMapper;
import com.mskyeye.handler.model.AisAlarmCalInfo;
import com.mskyeye.handler.model.LonLatInfo;
import com.mskyeye.handler.model.RadarAlarmCalInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName:QueryRadarAlarmCalInfoTask
 * @Description:10s定时查询雷达预警、AIS告警计算信息
 * @Author:R.Gong
 * @Date:2023/7/19 15:57
 * @Version:1.0
 **/
@Component
public class QueryAlarmCalInfoTask {

    @Autowired
    private YzRadarAlarmCalInfoMapper yzRadarAlarmCalInfoMapper;

    @Autowired
    private YzAisAlarmConfigMapper yzAisAlarmConfigMapper;

    @Scheduled(fixedDelay = 2000)
    public void alarmCalInfo() throws Exception {
        List<RadarAlarmCalInfo> radarAlarmList = yzRadarAlarmCalInfoMapper.selectRadarAlarmCalInfoList();
        GlobalResources.radarAlarmCalInfoMap.clear();
        if(radarAlarmList != null && !radarAlarmList.isEmpty()){
            List<RadarAlarmCalInfo> tmpList = radarAlarmList.stream().map(obj->{
                String pointStr = obj.getPoints();
                String[] lonLatArray = pointStr.split("_");
                List<LonLatInfo> lonLatInfoList = new ArrayList<>();
                for(String str: lonLatArray){
                    String[] tmpArray = str.split(",");
                    LonLatInfo lonLatInfo = new LonLatInfo();
                    if(StringUtil.isNotEmpty(tmpArray[0]) && StringUtil.isNotEmpty(tmpArray[1])){
                        lonLatInfo.setLon(Double.parseDouble(tmpArray[0]));
                        lonLatInfo.setLat(Double.parseDouble(tmpArray[1]));
                        lonLatInfoList.add(lonLatInfo);
                    }else {
                        throw new IllegalArgumentException("雷达预警区的经纬度错误");
                    }
                }
                obj.setPointsLonLat(lonLatInfoList);
                return obj;
            }).collect(Collectors.toList());
            //按组织ID进行分组并赋值给Map,组织ID是key
            GlobalResources.radarAlarmCalInfoMap.putAll(tmpList.stream().collect(Collectors.groupingBy(RadarAlarmCalInfo::getDeptId)));
            //更新雷达code和组织ID对应Map
            for(RadarAlarmCalInfo tmpInfo:radarAlarmList){
                GlobalResources.code2DeptIdMap.put(tmpInfo.getCode(),tmpInfo.getDeptId());
            }
        }

        List<AisAlarmCalInfo> aisAlarmList = yzAisAlarmConfigMapper.selectAisAlarmCalInfoList();
        GlobalResources.aisAlarmCalInfoMap.clear();
        if(StringUtil.isNotEmpty(aisAlarmList) && aisAlarmList.size() != 0){
            for(AisAlarmCalInfo aisAlarmCalInfo:aisAlarmList){
                GlobalResources.aisAlarmCalInfoMap.put(aisAlarmCalInfo.getMmsi(),aisAlarmCalInfo);
            }
        }
    }
}
