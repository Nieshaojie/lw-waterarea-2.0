package com.mskyeye.trace.cron;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mskyeye.trace.model.FishingDetectInfo;
import com.mskyeye.trace.model.YzAiCruiseInfo;
import com.mskyeye.trace.model.YzAiPointInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.proc.DhCameraProc;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.mskyeye.trace.common.GlResources.*;

/**
 * @ClassName:AiCruiseTask
 * @Description:AI巡航定时任务
 * @Author:R.Gong
 * @Date:2023/9/10 15:53
 * @Version:1.0
 **/
@Component
public class AiCruiseTask {

    @Autowired
    private HpCameraProc hpCameraProc;
    @Autowired
    private HkCameraProc hkCameraProc;
    @Autowired
    private DhCameraProc dhCameraProc;

    @Autowired
    private RedisCache redisCache;

    /**
     * 1000ms更新一次巡航点位状态
     */
    @Scheduled(fixedDelay = 1000)
    public void AiCruiseStatus() throws Exception {
        Map<Long, String> map = redisCache.getCacheObject(CRUISE_STATE);
        Map<Long, YzAiCruiseInfo> map1 = new HashMap<>();
        for(Map.Entry<Long,String> entry:map.entrySet()){
            map1.put(entry.getKey(),JSON.parseObject(entry.getValue(),YzAiCruiseInfo.class));
        }
        GL_CruiseMap.clear();
        GL_CruiseMap.putAll(map1);
        if (!GL_CruiseMap.isEmpty()) {
            Iterator<Map.Entry<Long, YzAiCruiseInfo>> iterator = GL_CruiseMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, YzAiCruiseInfo> entry = iterator.next();
                YzAiCruiseInfo yzAiCruiseInfo = entry.getValue();
                if(yzAiCruiseInfo.getCurPointIndex() == -1){
                    continue;
                }
                List<YzAiPointInfo> list =  yzAiCruiseInfo.getPointsInfoList();
                YzAiPointInfo yzAiPointInfo = list.get(yzAiCruiseInfo.getCurPointIndex());
                if (yzAiCruiseInfo.getStatus() == 0) {
                    YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(yzAiCruiseInfo.getCameraId());
                    if(yzCameraInfo == null){
                        continue;
                    }
                    Integer clock = yzAiCruiseInfo.getClock() + 1;//时钟加1s
                    yzAiCruiseInfo.setClock(clock);
                    //第1s发送预置点引导指令
                    if (clock == 1) {
                        switch (yzCameraInfo.getManu()) {
                            case "hp":
                                hpCameraProc.ptzControl(yzCameraInfo, yzAiPointInfo.getPVal(),
                                        yzAiPointInfo.getTVal(), yzAiPointInfo.getZVal());
                                break;
                            case "hik":
                                hkCameraProc.ptzControl(yzCameraInfo, yzAiPointInfo.getPVal(),
                                        yzAiPointInfo.getTVal(), yzAiPointInfo.getZVal());
                                break;
                            case "dh":
                                break;
                        }
                        GL_CurPointInfoMap.put(yzCameraInfo.getId(),yzAiPointInfo);
                    }
                    //第20s发送开启识别指令
                    else if (clock == 20) {
                        FishingDetectInfo fishingDetectInfo = new FishingDetectInfo();
                        fishingDetectInfo.setIp(yzCameraInfo.getIp());
                        switch (yzCameraInfo.getManu()) {
                            case "hp":
                                fishingDetectInfo.setChannel(1);
                                break;
                            case "hik":
                                fishingDetectInfo.setChannel(1);
                                break;
                            case "dh":
                                fishingDetectInfo.setChannel(0);
                                break;
                        }
                        fishingDetectInfo.setPresetNum(Math.toIntExact(yzAiCruiseInfo.getPointsInfoList()
                                .get(yzAiCruiseInfo.getCurPointIndex()).getId()));
                        fishingDetectInfo.setStatus(0);
                        redisCache.pushMsg(DETECT_KEY, JSONObject.toJSONString(fishingDetectInfo));

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                        String formattedDateTime = now.format(formatter);
//                        System.out.println(formattedDateTime + "***************发送指令:"+ fishingDetectInfo);
                    }
                    //第260s发送停止识别指令
                    else if (clock == 260) {
                        FishingDetectInfo fishingDetectInfo = new FishingDetectInfo();
                        fishingDetectInfo.setIp(yzCameraInfo.getIp());
                        switch (yzCameraInfo.getManu()) {
                            case "hp":
                                fishingDetectInfo.setChannel(1);
                                break;
                            case "hik":
                                fishingDetectInfo.setChannel(1);
                                break;
                            case "dh":
                                fishingDetectInfo.setChannel(0);
                                break;
                        }
                        fishingDetectInfo.setPresetNum(Math.toIntExact(yzAiCruiseInfo.getPointsInfoList()
                                .get(yzAiCruiseInfo.getCurPointIndex()).getId()));
                        fishingDetectInfo.setStatus(1);
                        redisCache.pushMsg(DETECT_KEY, JSONObject.toJSONString(fishingDetectInfo));

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                        String formattedDateTime = now.format(formatter);
//                        System.out.println(formattedDateTime + "***************发送指令:"+ fishingDetectInfo);
                    }
                    //第280s置零clock,切换预置点
                    else if (clock == 280) {
                        yzAiCruiseInfo.setClock(0);
                        yzAiCruiseInfo.setCurPointIndex((yzAiCruiseInfo.getCurPointIndex() + 1)
                                % yzAiCruiseInfo.getPointsInfoList().size());
                        GL_CurPointInfoMap.remove(yzCameraInfo.getId());
                    }
                    //修改信息
//                    GL_CruiseMap.put(entry.getKey(),yzAiCruiseInfo);
                   entry.setValue(yzAiCruiseInfo);
                    Map<Long,String> map2 = new HashMap<>();
                    for(Map.Entry<Long, YzAiCruiseInfo> entry1:GL_CruiseMap.entrySet()){
                        map2.put(entry1.getKey(),JSON.toJSONString(entry1.getValue()));
                    }
                    redisCache.setCacheObject(CRUISE_STATE,map2);
                }
            }
        }
    }
}
