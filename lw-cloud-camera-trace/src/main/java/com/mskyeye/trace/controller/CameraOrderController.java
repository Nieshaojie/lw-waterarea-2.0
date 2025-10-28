package com.mskyeye.trace.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.camera.gpl.sdk.GplNetSDK;
import com.mskyeye.trace.model.*;
import com.mskyeye.trace.netty.control.service.CameraLensControl;
import com.mskyeye.trace.proc.DhCameraProc;
import com.mskyeye.trace.proc.GplCameraProc;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.utils.AjaxResult;
import com.mskyeye.trace.utils.DisAndAngleUtils;
import com.mskyeye.trace.utils.RedisCache;
import com.mskyeye.trace.utils.StringUtil;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mskyeye.trace.common.GlResources.*;
import static java.lang.Math.toDegrees;


/**
 * 光电指令类Controller
 *
 * @author ruoyi
 * @date 2023-06-15
 */
@RestController
@RequestMapping("/camera_order")
public class CameraOrderController {

    private static final Integer MAN_TRACE = 1;//联动跟踪
    private static final Integer BOX_TRACE = 2;//框选跟踪
    private static final Integer PHOTO_TRACE = 3;//图像跟踪
    private static final Logger log = LoggerFactory.getLogger(CameraOrderController.class);
    @Autowired
    private HpCameraProc hpCameraProc;
    @Autowired
    private HkCameraProc hkCameraProc;
    @Autowired
    private DhCameraProc dhCameraProc;
    @Autowired
    private GplCameraProc gplCameraProc;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private CameraLensControl GplControl;

    /**
     * 跟踪命令接口
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/trace_order")
    public AjaxResult traceOrder(@RequestBody TraceProInfo traceProInfo) {
        Integer traceType = traceProInfo.getTraceType();
        Boolean bResult = false;
        try {
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
            traceProInfo.setManu(yzCameraInfo.getManu());

            if (StringUtil.isEmpty(yzCameraInfo)) {
                return AjaxResult.error("相机未登录");
            }
            //判断是否处于AI巡航
            for (Map.Entry<Long, TraceProInfo> entry : GL_TraceInfoMap.entrySet()) {
                TraceProInfo traceProInfo1 = entry.getValue();
                if (traceProInfo1.getCameraId() == yzCameraInfo.getId()) {
                    if (traceProInfo1.getTraceType() == 5) {
                        return AjaxResult.error("请关闭该相机的AI巡航");
                    } else if (traceProInfo1.getTraceType() == 6) {
                        return AjaxResult.error("请关闭该相机的雷光警戒");
                    } else if (traceProInfo1.getTraceType() == 7){
                        return AjaxResult.error("等待取证完毕后关闭该相机的雷光警戒");
                    }
                }
            }

            //取消跟踪指令
            if (!traceProInfo.getbTracking()) {
                //如果相机没有在跟踪，不做任何操作
                if (GL_TraceInfoMap.isEmpty() || !GL_TraceInfoMap.containsKey(yzCameraInfo.getId())) {
                    GplControl.stopAiTrack(yzCameraInfo);
                    return AjaxResult.success();
                }
                cancelTraceOrder(traceProInfo, yzCameraInfo);
                GL_TraceInfoMap.remove(traceProInfo.getCameraId());
                return AjaxResult.success();
            }
            //非取消跟踪指令先要取消上一跟踪
            if (!GL_TraceInfoMap.isEmpty() && GL_TraceInfoMap.containsKey(yzCameraInfo.getId())) {
                TraceProInfo oldTraceProInfo = GL_TraceInfoMap.get(traceProInfo.getCameraId());
                cancelTraceOrder(oldTraceProInfo, yzCameraInfo);
                GL_TraceInfoMap.remove(oldTraceProInfo.getCameraId());
            }
            //如果该相机处于不同类型的跟踪状态，需要先取消跟踪
//            if (GL_TraceInfoMap.containsKey(traceProInfo.getCameraId()) &&
//                    GL_TraceInfoMap.get(traceProInfo.getCameraId()).getTraceType() != traceProInfo.getTraceType()) {
//                return AjaxResult.error("请先取消该相机的跟踪");
//            }
            switch (traceType) {
                //1:联动跟踪 2:框选跟踪 3:图像跟踪 4:光电引导 6:雷光警戒
                case 1:
                    if ((traceProInfo.getManu().equals("dh")) || (traceProInfo.getManu().equals("hik"))) {
                        return AjaxResult.error("该相机暂不支持");
                    }
                    bResult = sendR_CTraceOrder(traceProInfo);
                    break;
                case 2:
                    bResult = sendBoxOrder(traceProInfo);
                    break;
                case 3:
                    System.out.println("图像跟踪指令");
                    bResult = sendPhotoOrder(traceProInfo);
                    break;
                case 4:
                    bResult = ctrlCameraByLonLat(traceProInfo);
                    break;
                case 6:
                    bResult = true;
                case 8://高普乐AI跟踪
                   /* //经纬高跟踪指令
                    bResult = sendTrackingCtrl(traceProInfo);*/
                    bResult =sendAiTraceOrder(traceProInfo);
                    break;
            }
            if (bResult == true) {
                //光电引导不是跟踪，不需要加入跟踪缓存
                if (traceType != 4) {
                    traceProInfo.setbTracking(true);
                    TraceProInfo newTraceProInfo = new TraceProInfo();
                    newTraceProInfo = traceProInfo;
                    GL_TraceInfoMap.put(newTraceProInfo.getCameraId(), newTraceProInfo);
                }
                return AjaxResult.success();
            } else {
                return AjaxResult.error();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    @PostMapping("/cruise_order")
    public AjaxResult cruiseOrder(@RequestParam(value = "cruiseId") Long cruiseId,
                                  @RequestParam(value = "status") Integer status) throws Exception {
        try {
            YzAiCruiseInfo yzAiCruiseInfo = GL_CruiseMap.get(cruiseId);
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(yzAiCruiseInfo.getCameraId());
            Long cameraId = yzCameraInfo.getId();
            if (GL_TraceInfoMap.containsKey(cameraId)) {
                if (GL_TraceInfoMap.get(cameraId).getTraceType() == 1 ||
                        GL_TraceInfoMap.get(cameraId).getTraceType() == 2 ||
                        GL_TraceInfoMap.get(cameraId).getTraceType() == 3) {
                    return AjaxResult.error("请先关闭该相机的跟踪");
                } else if (GL_TraceInfoMap.get(cameraId).getTraceType() == 6 ||
                        GL_TraceInfoMap.get(cameraId).getTraceType() == 7) {
                    return AjaxResult.error("请先关闭该相机的雷光警戒");
                }
            }
            if (status == 1) {
                //发送取消巡航指令
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
                fishingDetectInfo.setStatus(status);
                redisCache.pushMsg(DETECT_KEY, JSONObject.toJSONString(fishingDetectInfo));
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                String formattedDateTime = now.format(formatter);
//                System.out.println(formattedDateTime + "***************发送指令:" + fishingDetectInfo);
                //修改巡航状态
                yzAiCruiseInfo.setCurPointIndex(-1);
                //删除用于显示的状态信息
                GL_TraceInfoMap.remove(cameraId);
            } else if (status == 0) {
                yzAiCruiseInfo.setCurPointIndex(0);
                TraceProInfo traceProInfo = new TraceProInfo();
                //新增用于显示的状态信息
                traceProInfo.setManu(yzCameraInfo.getManu());
                traceProInfo.setTraceType(5);
                GL_TraceInfoMap.put(cameraId, traceProInfo);
            }
            yzAiCruiseInfo.setClock(0);
            yzAiCruiseInfo.setStatus(status);
            GL_CruiseMap.put(yzAiCruiseInfo.getId(), yzAiCruiseInfo);
            Map<Long, String> map = new HashMap<>();
            for (Map.Entry<Long, YzAiCruiseInfo> entry : GL_CruiseMap.entrySet()) {
                Long key = entry.getKey();
                String value = JSON.toJSONString(entry.getValue());
                map.put(key, value);
            }
            redisCache.setCacheObject(CRUISE_STATE, map);
            return AjaxResult.success();
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 根据PTZ值控制相机
     * 前端入口
     * @param cameraId
     * @param pVal
     * @param tVal
     * @param zVal
     * @return
     * @throws Exception
     */
    @PostMapping("/ptz_ctrl")
    public AjaxResult ptzCtrl(@RequestParam(value = "cameraId") Long cameraId,
                              @RequestParam(value = "pVal") Double pVal,
                              @RequestParam(value = "tVal") Double tVal,
                              @RequestParam(value = "zVal") Double zVal) throws Exception {
        try {
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(cameraId);

            if (GL_TraceInfoMap.containsKey(yzCameraInfo.getId()) &&
                    (GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType() == 2 ||
                            GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType() == 3)) {
                return AjaxResult.error("请先关闭该相机的跟踪");
            }
            for (Map.Entry<Long, YzAiCruiseInfo> entry : GL_CruiseMap.entrySet()) {
                YzAiCruiseInfo yzAiCruiseInfo = entry.getValue();
                if (yzAiCruiseInfo.getCameraId() == yzCameraInfo.getId() &&
                        yzAiCruiseInfo.getStatus() == 0) {
                    return AjaxResult.error("请先关闭该相机的AI巡航");
                }
            }
            switch (yzCameraInfo.getManu()) {
                case "hp":
                    hpCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zVal);
                    break;
                case "hik":
                    hkCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zVal);
                    break;
                case "dh":
                    dhCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zVal);
                    break;
                case "gpl":
                    gplCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zVal);
                    break;
            }
            return AjaxResult.success();

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 强光指令
     * @param cameraOrder
     * @return
     */
    @PostMapping("/light_order")
    public AjaxResult lightOrder(@RequestBody CameraOrder cameraOrder){
        try {
            gplCameraProc.lightControl(GL_CameraInfoMap.get(cameraOrder.getId()),cameraOrder.getCommand());
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }

    }

    /**
     * 发送雷光跟踪指令
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean sendR_CTraceOrder(TraceProInfo traceProInfo) throws Exception {
        //相机转到该经纬度
        ctrlCameraByLonLat(traceProInfo);
        //标记跟踪
        traceProInfo.setTraceType(1);
//        TimeUnit.MILLISECONDS.sleep(3000);
        return true;
    }

    /**
     * 发送AI跟踪指令
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean sendAiTraceOrder(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        //调用云台前先关闭ai跟踪
        GplControl.stopAiTrack(yzCameraInfo);
        //相机转到该经纬度
        ctrlCameraByLonLat(traceProInfo);
        //延迟2秒，开启ai跟踪
        // ===== 延迟2秒，开启AI自动跟踪 =====
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                log.info("三秒后自动开启AI跟踪...");
                GplControl.startAiTrack(yzCameraInfo);
                log.info("AI自动跟踪指令已发送");
            } catch (Exception e) {
                log.error("开启AI自动跟踪失败", e);
            }
        }).start();
        //标记ai跟踪
        traceProInfo.setTraceType(8);
//        TimeUnit.MILLISECONDS.sleep(3000);
        return true;
    }

    /**
     * 发送框选跟踪指令
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean sendBoxOrder(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        if (traceProInfo.getManu().equals("hp")) {
            Integer x = (int) (traceProInfo.getLeft() * 640);
            Integer y = (int) (traceProInfo.getTop() * 512);
            Integer w = (int) ((traceProInfo.getRight() - traceProInfo.getLeft()) * 640);
            Integer h = (int) ((traceProInfo.getBottom() - traceProInfo.getTop()) * 512);
            if (traceProInfo.getChannelId() == 1) {
                traceProInfo.setChannelId(2);
            } else if (traceProInfo.getChannelId() == 2) {
                traceProInfo.setChannelId(1);
            }
            //发送框选跟踪指令
            hpCameraProc.boxTrackCtrl(yzCameraInfo, traceProInfo.getbTracking(), traceProInfo.getChannelId(), x, y, w, h);
        } else if (traceProInfo.getManu().equals("hik")) {
            char[] szUrl;
            char[] pBuf;
            int positionX = (int) (traceProInfo.getLeft() * 255);
            int positionY = (int) ((1 - traceProInfo.getTop()) * 255);
            int positionX2 = (int) (traceProInfo.getRight() * 255);
            int positionY2 = (int) ((1 - traceProInfo.getBottom()) * 255);
            //发送框选跟踪指令
            hkCameraProc.boxTrackCtrl(yzCameraInfo, traceProInfo.getChannelId(), positionX, positionY, positionX2, positionY2);
        } else if (traceProInfo.getManu().equals("dh")) {
            return false;
        } else {
            return false;
        }
        traceProInfo.setTraceType(2);
        return true;
    }

    /**
     * 发送经纬高引导指令
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean sendTrackingCtrl(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        if (traceProInfo.getManu().equals("hp")) {
            if (traceProInfo.getChannelId() == 1) {
                traceProInfo.setChannelId(2);
            } else if (traceProInfo.getChannelId() == 2) {
                traceProInfo.setChannelId(1);
            }
            //发送框选跟踪指令
            hpCameraProc.trackingCtrl(yzCameraInfo, traceProInfo);
        } else if (traceProInfo.getManu().equals("hik")) {
            return false;
        } else if (traceProInfo.getManu().equals("dh")) {
            return false;
        } else {
            return false;
        }
        traceProInfo.setTraceType(2);
        return true;
    }

    /**
     * 发送图像跟踪指令
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean sendPhotoOrder(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
//        System.out.println("图像跟踪相机信息：{}"+yzCameraInfo.toString());
        //相机转到该经纬度
        ctrlCameraByLonLat(traceProInfo);
        if (traceProInfo.getManu().equals("hp")) {
            /*if (traceProInfo.getChannelId() == 1) {
                traceProInfo.setChannelId(2);
            } else if (traceProInfo.getChannelId() == 2) {
                traceProInfo.setChannelId(1);
            }*/
            //发送图像跟踪指令
            hpCameraProc.photoTrackingCtrl(yzCameraInfo, traceProInfo.getbTracking(), traceProInfo.getChannelId());
            try {
                Thread.sleep(1500); // 延迟1.5秒（1500毫秒）
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                e.printStackTrace(); // 可选：打印异常栈信息
            }
            //相机转到该经纬度
            ctrlCameraByLonLatToHP(traceProInfo);
        } else if (traceProInfo.getManu().equals("hik")) {
            //注:海康相机的图像跟踪需要先发送【起始跟踪指令】,然后定时发送【连续跟踪指令】
            if (!hkCameraProc.photoTraceStart(traceProInfo, yzCameraInfo)) {
                return false;
            }
        } else if (traceProInfo.getManu().equals("dh")) {
            return false;
        } else {
            return false;
        }
        traceProInfo.setTraceType(3);
        return true;
    }


    /**
     * 光电引导
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean ctrlCameraByLonLat(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        //偏移校准值
        double pCorVal = yzCameraInfo.getAngle();
        double tCorVal = yzCameraInfo.getCurTVal();
        double zFixVal = yzCameraInfo.getzVal();
        double height = yzCameraInfo.getHeight();
        double t_Val = yzCameraInfo.gettVal();
        //相对于相机的角度
//        double dBear = DisAndAngleUtils.get_bearing(yzCameraInfo.getLat().doubleValue(),
//                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        double dBear = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        //相对于相机的距离
//        double dis = DisAndAngleUtils.get_distance(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
//                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        if(yzCameraInfo.getName().equals("双廊镇")){
            if(dis >= 5500.0 &&  dis <= 9000.0 && dBear >= 203.0 && dBear <= 220.0){
                dBear = (dBear + 0.65)%360.0;
                System.out.println("双廊镇相机当前引导角度：" + dBear);
            }
        }
        //计算P值
        double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;
        pVal = pVal < 0 ? 360 + pVal : pVal;
        //TODO 计算出的T值
        // 目标与相机高度差
        double targetHeight = traceProInfo.getTraceAlt() == null ? 0 : traceProInfo.getTraceAlt();
        double heightDiff = targetHeight - height;
        Double tVal = calTVal(yzCameraInfo.getName(),dis,dBear);
        if(tVal == null){
            if (yzCameraInfo.getManu().equals("gpl")) {
                tVal = Math.toDegrees(Math.atan2(heightDiff, dis)) + t_Val;
//                tVal = tVal < 0 ? 0 : tVal;
                System.out.println("没有用曲线拟合方法计算T值");
            }else{
                tVal = -1 * toDegrees(Math.atan2(heightDiff, dis))+ t_Val;
            }
        }
        //计算Z值
        if(yzCameraInfo.getManu().equals("gpl")){
            Double zVal = calcZoomByDistance(dis);
            if(zVal != null){
                zFixVal = zVal;
            }
        }
        if(yzCameraInfo.getManu().equals("hp")){
            Double zVal = calZValHP(dis);
            if(zVal != null){
                zFixVal = zVal;
            }
        }
//        double tVal = -1* toDegrees(asin(height/dis));//旧的方法
        System.out.println("计算出的方位角 dBear: " + dBear);
        System.out.println("相机偏移校准 pCorVal: " + pCorVal+" ---- 相机偏移校准 tCorVal: " + t_Val);
        System.out.println("最终 P 角: " + pVal +"     最终 T 角: " + tVal);

        if (yzCameraInfo.getManu().equals("hik")) {
            hkCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("dh")) {
            dhCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("hp")) {
            hpCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        } else if (yzCameraInfo.getManu().equals("gpl")) {
            gplCameraProc.ptzControlPD(yzCameraInfo, pVal, tVal, zFixVal);
        }
        traceProInfo.setTraceType(4);
        return true;
    }

    /**
     * 光电引导
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private Boolean ctrlCameraByLonLatToHP(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        //偏移校准值
        double pCorVal = yzCameraInfo.getAngle();
        double zFixVal = yzCameraInfo.getzVal();
        double height = yzCameraInfo.getHeight();
        double t_Val = yzCameraInfo.gettVal();
        //相对于相机的角度
        double dBear = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        //相对于相机的距离
        double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        //计算P值
        double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;
        pVal = pVal < 0 ? 360 + pVal : pVal;
        //TODO 计算出的T值
        Double tVal = -1 * toDegrees(Math.atan2(height, dis))+ t_Val + 6;
        //计算Z值
            Double zVal = calZValHP(dis);
            if(zVal != null){
                zFixVal = zVal;
            }
        System.out.println("计算出的方位角 dBear: " + dBear);
        System.out.println("相机偏移校准 pCorVal: " + pCorVal+" ---- 相机偏移校准 tCorVal: " + t_Val);
        System.out.println("最终 P 角: " + pVal +"     最终 T 角: " + tVal);

        hpCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);
        traceProInfo.setTraceType(4);
        return true;
    }


    /**
     * 取消跟踪
     *
     * @param traceProInfo
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    private TraceProInfo cancelTraceOrder(TraceProInfo traceProInfo, YzCameraInfo yzCameraInfo) throws Exception {
        Integer traceType = GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType();
        if (traceProInfo.getManu().equals("hp")) {
            if (traceType == 1) {
                hpCameraProc.aziControl(yzCameraInfo, -1, true);
            } else if (traceType == 2) {
                hpCameraProc.boxTrackCtrl(yzCameraInfo, false, traceProInfo.getChannelId(), 0, 0, 0, 0);
            } else if (traceType == 3) {
                hpCameraProc.photoTrackingCtrl(yzCameraInfo, false, traceProInfo.getChannelId());
                //下面这个操作必须添加
                hpCameraProc.boxTrackCtrl(yzCameraInfo, false, traceProInfo.getChannelId(), 0, 0, 0, 0);
            }
        } else if (traceProInfo.getManu().equals("hik")) {
            //使用最基本的云台控制左移<停>指令结束跟踪
            yzCameraInfo.getHkNetSDK().NET_DVR_PTZControlWithSpeed_Other(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())),
                    new NativeLong(1L), 21, 1, 1);
        } else if (traceProInfo.getManu().equals("dh")) {

        } else if (traceProInfo.getManu().equals("gpl")) {
            //gplCameraProc.aziControl(yzCameraInfo, -1, true);
            //取消AI跟踪
            if (traceType == 8) {
                GplControl.stopAiTrack(yzCameraInfo);
            }
        } else {
            return null;
        }
        traceProInfo.setTargetId(0L);
        traceProInfo.setbTracking(false);
        traceProInfo.setTraceType(0);
        return traceProInfo;
    }

    /**
     * gpl云台控制相机
     * 前端入口
     * @param cameraId
     * @return
     * @throws Exception
     */
    @PostMapping("/gpl_ptz_ctrl")
    public AjaxResult ptzControl (@RequestParam(value = "cameraId") Long cameraId,
                              @RequestParam(value = "lRealHandle") int lRealHandle,
                              @RequestParam(value = "dwPTZCommand") int dwPTZCommand,
                              @RequestParam(value = "dwStop") int dwStop) throws Exception {
        try {
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(cameraId);

            if (GL_TraceInfoMap.containsKey(yzCameraInfo.getId()) &&
                    (GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType() == 2 ||
                            GL_TraceInfoMap.get(yzCameraInfo.getId()).getTraceType() == 3)) {
                return AjaxResult.error("请先关闭该相机的跟踪");
            }
            for (Map.Entry<Long, YzAiCruiseInfo> entry : GL_CruiseMap.entrySet()) {
                YzAiCruiseInfo yzAiCruiseInfo = entry.getValue();
                if (yzAiCruiseInfo.getCameraId() == yzCameraInfo.getId() &&
                        yzAiCruiseInfo.getStatus() == 0) {
                    return AjaxResult.error("请先关闭该相机的AI巡航");
                }
            }
            switch (yzCameraInfo.getManu()) {
                case "hp":
                    break;
                case "hik":
                    break;
                case "dh":
                    break;
                case "gpl":
                    boolean b = yzCameraInfo.getGplNetSDK().NET_DVR_PTZControl(lRealHandle, dwPTZCommand, GplNetSDK.STOP);
                    System.out.println("gpl云台控制返回结果："+b);
                    break;
            }
            return AjaxResult.success();

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }


    /*public static void main(String[] args) {

        int realHandle = 1;
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(cameraId);
        boolean b = yzCameraInfo.getGplNetSDK().NET_DVR_PTZControl(realHandle, 0, GplNetSDK.STOP);
    }*/
}
