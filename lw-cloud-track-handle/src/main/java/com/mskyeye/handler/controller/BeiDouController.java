package com.mskyeye.handler.controller;

import com.mskyeye.handler.model.PatrolData;
import com.mskyeye.handler.model.PatrolUserInfo;
import com.mskyeye.handler.utils.AjaxResult;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.mskyeye.handler.common.GlobalResources.beidouTrackMap;
import static com.mskyeye.handler.common.GlobalResources.trackHandleQueue;

/**
 * @ClassName:BeiDouController
 * @Description:北斗巡护系统接口
 * @Author:R.Gong
 * @Date:2024/5/30 13:59
 * @Version:1.0
 **/
@RestController
@RequestMapping("/patrol")
public class BeiDouController {

    /**
     * 上报所有点
     * @param patrolDatas
     * @return
     */
    @PostMapping("/rptAllPort")
    public AjaxResult rptAllPort(@RequestBody List<PatrolData> patrolDatas) {

        LwTrackPacket lwTrackPacket = new LwTrackPacket();
        lwTrackPacket.setTIME(System.currentTimeMillis());
        List<Content> cntList = new ArrayList<>();
        for(PatrolData patrolData:patrolDatas){
//            System.out.println(patrolData);
            PatrolUserInfo patrolUserInfo = patrolData.getPatrolUserInfo();
            //判断是否在线,是否为合法的经纬度
            if(patrolData.getOnline().equals("1") &&
                    isValidLongitude(patrolData.getLongitude()) && isValidLatitude(patrolData.getLatitude())){
                //TODO 暂时当成AIS
//                AisTrackCache aisTrackCache = new AisTrackCache();
//                aisTrackCache.setTargetId(patrolUserInfo.getId());
//                aisTrackCache.setShipLon(parseDoubleWithPrecision(patrolData.getLongitude(),7));
//                aisTrackCache.setShipLat(parseDoubleWithPrecision(patrolData.getLatitude(),7));
//                aisTrackCache.setRefreshTime(System.currentTimeMillis());
//                aisTrackMap.put(aisTrackCache.getTargetId(),aisTrackCache);

                Content cnt = new Content();
                cnt.setTID(patrolUserInfo.getId());
                cnt.setSTATIONID(Integer.parseInt(patrolData.getStationId()));
                cnt.setSOURCE(5);
                cnt.setSTATUS(1);
                cnt.setMMSI(-1L);
                cnt.setSHIPNAME(patrolUserInfo.getName());
                cnt.setLON(parseDoubleWithPrecision(patrolData.getLongitude(),7));
                cnt.setLAT(parseDoubleWithPrecision(patrolData.getLatitude(),7));
                cntList.add(cnt);
            }

        }
        if(!cntList.isEmpty()){
            lwTrackPacket.setITEM(cntList);
            trackHandleQueue.offer(lwTrackPacket);
            beidouTrackMap.put(cntList.get(0).getTID(),lwTrackPacket);
        }
        return AjaxResult.success();
    }


    // 判断是否为合法经度
    private boolean isValidLongitude(String longitude) {
        String regEx = "^\\d{1,3}\\.\\d+$";
        return Pattern.matches(regEx, longitude);
    }

    // 判断是否为合法纬度
    private boolean isValidLatitude(String latitude) {
        String regEx = "^\\d{1,2}\\.\\d+$";
        return Pattern.matches(regEx, latitude);
    }

    // 将字符串解析为保留指定有效数字位数的Double型
    private double parseDoubleWithPrecision(String numberStr, int precision) {
        DecimalFormat df = new DecimalFormat("#." + new String(new char[precision]).replace('\0', '#'));
        return Double.parseDouble(df.format(Double.parseDouble(numberStr)));
    }
}
