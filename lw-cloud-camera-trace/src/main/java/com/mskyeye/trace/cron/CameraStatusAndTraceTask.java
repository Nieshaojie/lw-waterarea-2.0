package com.mskyeye.trace.cron;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.mskyeye.trace.camera.dhkj.sdk.DhNetSDK;
import com.mskyeye.trace.camera.dhkj.struct.DH_PTZ_LOCATION_INFO;
import com.mskyeye.trace.camera.gpl.sdk.GplNetSDK;
import com.mskyeye.trace.camera.gpl.struct.VS_PTZ_LOCATION_INFO;
import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_PTZPOS;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_XML_CONFIG_INPUT;
import com.mskyeye.trace.camera.hkws.struct.NET_DVR_XML_CONFIG_OUTPUT;
import com.mskyeye.trace.model.GplCmdSender;
import com.mskyeye.trace.model.LwCameraStatusPacket;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.mq.utils.MqConnectionUtil;
import com.mskyeye.trace.proc.DhCameraProc;
import com.mskyeye.trace.proc.GplCameraProc;
import com.mskyeye.trace.proc.HkCameraProc;
import com.mskyeye.trace.proc.HpCameraProc;
import com.mskyeye.trace.utils.DisAndAngleUtils;
import com.mskyeye.trace.utils.RedisCache;
import com.rabbitmq.client.AMQP;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mskyeye.trace.camera.dhkj.sdk.DhNetSDK.DH_DEVSTATE_PTZ_LOCATION;
import static com.mskyeye.trace.camera.gpl.sdk.GplNetSDK.GPL_DEVSTATE_PTZ_LOCATION;
import static com.mskyeye.trace.camera.hkws.sdk.HkNetSDK.NET_DVR_GET_PTZPOS;
import static com.mskyeye.trace.common.GlResources.*;
import static java.lang.Math.toDegrees;

/**
 * @ClassName:QueryCameraStatusTask
 * @Description:相机状态和跟踪任务
 * @Author:R.Gong
 * @Date:2023/8/7 18:58
 * @Version:1.0
 **/
@Component
@EnableAsync
//@Order(4)
public class CameraStatusAndTraceTask {

    private static final Logger log = LoggerFactory.getLogger(CameraStatusAndTraceTask.class);
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
    private MqConnectionUtil mqConnectionUtil;
    private DecimalFormat df = new DecimalFormat("#.##");

    // 设置消息的TTL为10秒
    private AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .expiration("10000")
            .build();

    private ExecutorService cameraExe; // 创建线程池，处理四种相机厂商信息

    public CameraStatusAndTraceTask() {
        // 初始化4个固定线程的线程池
        cameraExe = Executors.newFixedThreadPool(4);

        // 在应用程序关闭时关闭线程池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cameraExe.shutdown();
            try {
                if (!cameraExe.awaitTermination(30, TimeUnit.SECONDS)) {
                    cameraExe.shutdownNow();
                }
            } catch (InterruptedException e) {
                cameraExe.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }



    /**
     * 等待200ms查询一次相机PT值
     */
    @Scheduled(fixedDelay = 2000)
    @Async
    public void QueryPTVal() {
        if (GL_CameraInfoMap != null && !GL_CameraInfoMap.isEmpty()) {
            try {
                // 从Redis读取光电信息
                Map<String, String> statusMap = redisCache.getCacheObject(CAMERA_STATE_BY_ISC);
//                System.out.println("查询相机PT值方法中相机信息map+"+GL_CameraInfoMap.toString());
                for (YzCameraInfo yzCameraInfo : GL_CameraInfoMap.values()) {
                    if (!statusMap.containsKey(yzCameraInfo.getLightCode())) {
                        sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
                        continue;
                    } else if (statusMap.get(yzCameraInfo.getLightCode()).equals("离线")) {
                        sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
                        continue;
                    }
                    cameraExe.submit(() -> {
                        try {
                            processCameraInfoMultithreaded(yzCameraInfo, statusMap);
                        } catch (Exception e) {
                            // 记录异常日志
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 方法：处理HP相机信息
    private void processHpCameraInfo(YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception {
        // HP相机处理逻辑
        //获取PT值
        JSONObject jsonObject = hpCameraProc.ptInfoGet(yzCameraInfo);
        if (jsonObject == null || !jsonObject.containsKey("param") || !((JSONObject) jsonObject.get("param")).containsKey("angleInfoCurHor")) {
            //重新登录并获得相机token
//                            String token = hpCameraProc.userLogin(yzCameraInfo);
//                            yzCameraInfo.setLoginInfo("-1");
//                            GL_CameraInfoMap.put(entry.getKey(), yzCameraInfo);
            //登出
            hpCameraProc.userLogout(yzCameraInfo);
            hpCameraProc.reLoginHpCamera(yzCameraInfo);
            sendStatusToMq(infoToStatus(true, yzCameraInfo, statusMap));
            return;
        }
        Integer iPVal = 0;
        iPVal = (Integer) ((JSONObject) jsonObject.get("param")).get("angleInfoCurHor");
        yzCameraInfo.setOriPVal(iPVal.doubleValue() / 100);
        Double dPVal = (iPVal.doubleValue() / 100 + yzCameraInfo.getAngle() + 360.0) % 360.0;
        yzCameraInfo.setCurPVal(Double.valueOf(df.format(dPVal)));//方位要加上校准值
        Integer iTVal = ((Integer) ((JSONObject) jsonObject.get("param")).get("angleInfoCurVer"));
        //水平向上的正值
        if (iTVal >= 27000 && iTVal <= 36000) {
            yzCameraInfo.setCurTVal((36000 - iTVal.doubleValue()) / 100);
        }
        //水平向下的负值
        else if (iTVal >= 0 && iTVal <= 9000) {
            yzCameraInfo.setCurTVal(-1 * iTVal.doubleValue() / 100);
        }

        //获取Z值
        JSONObject jsonObject1 = hpCameraProc.visibleGetFocalInfo(yzCameraInfo);
        if (jsonObject1 == null || !jsonObject1.containsKey("param") || !((JSONObject) jsonObject1.get("param")).containsKey("camFocalCurFocal")) {
            sendStatusToMq(infoToStatus(true, yzCameraInfo, statusMap));
            return;
        } else {
            DecimalFormat df = new DecimalFormat("#.00");
            double curFocal = ((Integer) ((JSONObject) jsonObject1.get("param")).get("camFocalCurFocal")).floatValue() / 100;
            double roundedCurFocal = Double.parseDouble(df.format(curFocal));
            yzCameraInfo.setCurZVal(roundedCurFocal);
        }
        sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
        GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
    }

    // 方法：处理Hik相机信息
    //TODO 更换获取ptz值的接口
    private void processHikCameraInfo(YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception {
        // Hik相机处理逻辑
        HkNetSDK hkNetSDK = yzCameraInfo.getHkNetSDK();
        NativeLong loginId = new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo()));
//        System.out.println("当前海康相机信息:"+yzCameraInfo);
        if (loginId.intValue() == -1) {
            sendStatusToMq(infoToStatus(true, yzCameraInfo, statusMap));
            return;
        }
        NET_DVR_PTZPOS stPos = new NET_DVR_PTZPOS();
        int structSize = stPos.size(); // 结构体大小
        Pointer lpOutBuffer = stPos.getPointer(); // 结构体指针
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean bGetHikStatus = hkNetSDK.NET_DVR_GetDVRConfig(loginId, NET_DVR_GET_PTZPOS, new NativeLong(1), lpOutBuffer, structSize, ibrBytesReturned);
        //用以判断相机是否突然离线
        if (bGetHikStatus) {
            stPos.read();
        } else {
            yzCameraInfo.setLoginInfo("-1");
            sendStatusToMq(infoToStatus(true, yzCameraInfo, statusMap));
            GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
            return;
        }
        yzCameraInfo = hikCon2Angle(yzCameraInfo, stPos);  //获取海康相机ptz值并转换
        sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
//        System.out.println("当前已登录的海康相机信息:"+yzCameraInfo);
        GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
    }

    // 方法：处理Dahua相机信息
    private void processDhCameraInfo(YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception {
        // Dahua相机处理逻辑
                                DhNetSDK dhNetSDK = yzCameraInfo.getDhNetSDK();
                        DH_PTZ_LOCATION_INFO stPos = new DH_PTZ_LOCATION_INFO();
                        stPos.nChannelID = 0;
                        int structSize = stPos.size(); // 结构体大小
                        Pointer lpOutBuffer = stPos.getPointer(); // 结构体指针
                        IntByReference ibrBytesReturned = new IntByReference(0);
                        boolean ret = dhNetSDK.CLIENT_QueryRemotDevState(Long.valueOf(yzCameraInfo.getLoginInfo()), DH_DEVSTATE_PTZ_LOCATION, 0,
                                lpOutBuffer, structSize, ibrBytesReturned, 1000);
                        int error = dhNetSDK.CLIENT_GetLastError();
                        stPos.read();
                        yzCameraInfo = dhCon2Angle(yzCameraInfo, stPos);
        sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
        GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
    }

    // 方法：处理GPL相机信息
    private void processGplCameraInfo(YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception {
        //处理逻辑
                                GplNetSDK gplNetSDK = yzCameraInfo.getGplNetSDK();
                        VS_PTZ_LOCATION_INFO stPos = new VS_PTZ_LOCATION_INFO();
                        stPos.nChannelID = 0;
                        int structSize = stPos.size(); // 结构体大小
                        Pointer lpOutBuffer = stPos.getPointer(); // 结构体指针
                        IntByReference ibrBytesReturned = new IntByReference(0);
                        boolean ret = gplNetSDK.VSIF_QueryDevState(Long.valueOf(yzCameraInfo.getLoginInfo()), GPL_DEVSTATE_PTZ_LOCATION,
                                lpOutBuffer, structSize, ibrBytesReturned, 3000);
                        int error = gplNetSDK.VSIF_GetLastError();
                        stPos.read();
                        yzCameraInfo = gplCon2Angle(yzCameraInfo, stPos);
                sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
        GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
    }

    // 多线程执行不同厂商相机信息处理
    @Async
    public void processCameraInfoMultithreaded(YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception{
        String manu = yzCameraInfo.getManu();
        switch (manu) {
            case "hp":
                processHpCameraInfo(yzCameraInfo, statusMap);
                break;
            case "hik":
                processHikCameraInfo(yzCameraInfo, statusMap);
                break;
            case "dh":
                processDhCameraInfo(yzCameraInfo, statusMap);
                break;
            case "gpl":
                sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
//                processGplCameraInfo(yzCameraInfo, statusMap);
                log.info("gpl相机处理");
                break;
            default:
                sendStatusToMq(infoToStatus(false, yzCameraInfo, statusMap));
//                System.out.println("发送高普乐相机状态成功,当前时间:" + Utils.getDate());
        }
    }

    /**
     * 相机信息转成发送给消息队列的相机状态数据
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    private LwCameraStatusPacket infoToStatus(boolean bNull, YzCameraInfo yzCameraInfo, Map<String, String> statusMap) throws Exception {
        LwCameraStatusPacket lwCameraStatusPacket = new LwCameraStatusPacket();
        lwCameraStatusPacket.setIPCID(yzCameraInfo.getId());
        lwCameraStatusPacket.setIPCLAT(yzCameraInfo.getLat().doubleValue());
        lwCameraStatusPacket.setIPCLON(yzCameraInfo.getLon().doubleValue());
        lwCameraStatusPacket.setPVAL(yzCameraInfo.getCurPVal());
        lwCameraStatusPacket.setTVAL(yzCameraInfo.getCurTVal());
        lwCameraStatusPacket.setZVAL(yzCameraInfo.getCurZVal());
        lwCameraStatusPacket.setIPCNAME(yzCameraInfo.getName());
        lwCameraStatusPacket.setORIPVAL(yzCameraInfo.getOriPVal());
        lwCameraStatusPacket.setISAVALARM(yzCameraInfo.getIsAvAlarm());
        lwCameraStatusPacket.setISOPENLIGHT(yzCameraInfo.getIsLightOpen());
        lwCameraStatusPacket.setMANU(yzCameraInfo.getManu());
        TraceProInfo traceProInfo1 = redisCache.getCacheObject("LGJJ" + yzCameraInfo.getId());
        if(ObjectUtil.isNotEmpty(traceProInfo1)) {
            lwCameraStatusPacket.setTRALAT(traceProInfo1.getTraceLat());
            lwCameraStatusPacket.setTRALON(traceProInfo1.getTraceLon());
        }
        //TODO
//        lwCameraStatusPacket.setISOPENLIGHT(1);
        //TODO 1:联动跟踪 2:框选跟踪 3:智能跟踪
        TraceProInfo traceProInfo = GL_TraceInfoMap.get(yzCameraInfo.getId());
        if (traceProInfo == null || traceProInfo.getTraceType() == 4) {
            lwCameraStatusPacket.setIPCSTATUS("空闲");
        } else {
            switch (traceProInfo.getTraceType()) {
                case 1:
                    lwCameraStatusPacket.setIPCSTATUS("联动跟踪");
                    break;
                case 2:
                    lwCameraStatusPacket.setIPCSTATUS("框选跟踪");
                    break;
                case 3:
                    lwCameraStatusPacket.setIPCSTATUS("智能跟踪");
                    break;
                case 5:
                    lwCameraStatusPacket.setIPCSTATUS("AI巡航");
                    break;
                case 6:
                    lwCameraStatusPacket.setIPCSTATUS("雷光警戒");
                    break;
                case 7:
                    lwCameraStatusPacket.setIPCSTATUS("警戒抓拍");
                    break;
            }
        }
        if (!statusMap.containsKey(yzCameraInfo.getLightCode())) {
            lwCameraStatusPacket.setIPCSTATUS("离线");
        } else if (statusMap.get(yzCameraInfo.getLightCode()).equals("离线")) {
            lwCameraStatusPacket.setIPCSTATUS("离线");
        }
//        System.out.println("获取相机状态"+lwCameraStatusPacket);
        return lwCameraStatusPacket;
    }


    /**
     * 1000ms发送一次海康相机图像跟踪指令
     */
    @Scheduled(fixedRate = 1000)
    @Async
    public void cronHikPhotoTrace() {
        for (Map.Entry<Long, TraceProInfo> entry : GL_TraceInfoMap.entrySet()) {
            TraceProInfo traceProInfo = entry.getValue();
            if (traceProInfo.getManu().equals("hik") && traceProInfo.getTraceType() == 3) {
                YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
                hikPhotoTrace(traceProInfo, yzCameraInfo);
            }
        }
    }

    /**
     * 等待1000ms做一次联动跟踪跟踪
     */
    @Scheduled(fixedDelay = 1000)
    @Async
    public void cronR_CTrace() throws Exception {

        Iterator<Map.Entry<Long, TraceProInfo>> iter = GL_TraceInfoMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, TraceProInfo> entry = iter.next();
            TraceProInfo traceProInfo = entry.getValue();
            if(traceProInfo.getCameraId() == null){
                continue;
            }
            YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
            //新加,警戒抓拍状态,高普乐也需要联动跟踪
            if (traceProInfo.getTraceType() == 7 && yzCameraInfo.getManu().equals("gpl")) {
                //偏移校准值
                double pCorVal = yzCameraInfo.getAngle();
                double zFixVal = yzCameraInfo.getzVal();
                double height = yzCameraInfo.getHeight();
                double t_Val = yzCameraInfo.gettVal();
                //相对于相机的距离
                double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                        traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
                double dBear = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                        yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
                dBear = dBear < 0 ? 360 + dBear : dBear;
                double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;

                //TODO 计算出的T值
                Double tVal = calTVal(yzCameraInfo.getName(),dis,dBear);
                if(tVal == null){
                    if (yzCameraInfo.getManu().equals("gpl")) {
                        tVal = toDegrees(Math.atan2(height, dis)) + t_Val;
                        tVal = tVal < -90 ? 0 : tVal;
                        System.out.println("没有用曲线拟合方法计算T值——————原始t值："+toDegrees(Math.atan2(height, dis))+"————————补偿值："+t_Val+"————————最终t值："+tVal);
                    }else{
                        tVal = -1 * toDegrees(Math.atan2(height, dis));
                    }
                }
//                        gplCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);//SDK开发使用
                gplCameraProc.gplLinkTrace(yzCameraInfo, pVal, tVal, zFixVal);//串口开发使用
                continue;
            }
            if (traceProInfo.getTraceType() == 1) {
                //高普乐相机跟踪
                if (yzCameraInfo.getManu().equals("gpl")) {
                    //偏移校准值
                    double pCorVal = yzCameraInfo.getAngle();
                    double zFixVal = yzCameraInfo.getzVal();
                    double height = yzCameraInfo.getHeight();
                    //相对于相机的距离
                    double dis = DisAndAngleUtils.gis_Dis(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                            traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
                    double dBear = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                            yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
                    dBear = dBear < 0 ? 360 + dBear : dBear;
                    if(yzCameraInfo.getName().equals("双廊镇")){
                        if(dis >= 5500.0 &&  dis <= 9000.0 && dBear >= 203.0 && dBear <= 220.0){
                            dBear = (dBear + 0.65)%360.0;
                            System.out.println("双廊镇相机当前跟踪角度：" + dBear);
                        }
                    }
                    double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;

                    Double tVal = calTVal(yzCameraInfo.getName(),dis,dBear);
                    if(tVal == null){
                        if (yzCameraInfo.getManu().equals("gpl")) {
                            tVal = toDegrees(Math.atan2(height, dis));
                            tVal = tVal < 0 ? 0 : tVal;
                            System.out.println("没有用曲线拟合方法计算T值");
                        }else{
                            tVal = -1 * toDegrees(Math.atan2(height, dis));
                        }
                    }
                    //计算Z值
                    if(yzCameraInfo.getManu().equals("gpl")){
                        Double zVal = calZVal(dis);
                        if(zVal != null){
                            zFixVal = zVal;
                        }
                    }
//                        gplCameraProc.ptzControl(yzCameraInfo, pVal, tVal, zFixVal);//SDK开发使用
                    //TODO 暂时改成按当前的T值,跟踪时不再根据计算得出T值
//                    tVal = yzCameraInfo.getCurTVal();
                    gplCameraProc.gplLinkTrace(yzCameraInfo, pVal, tVal, zFixVal);//串口开发使用
                    continue;
                }

                //相机当前指示线角度
                double dBear1 = yzCameraInfo.getCurPVal();
                dBear1 = dBear1 < 0 ? 360 + dBear1 : dBear1;
                dBear1 = dBear1 > 360 ? dBear1 - 360 : dBear1;
                //目标位置角度
                double dBear2 = DisAndAngleUtils.gis_Angle(yzCameraInfo.getLat().doubleValue(),
                        yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
                dBear2 = dBear2 < 0 ? 360 + dBear2 : dBear2;
                dBear2 = dBear2 > 360 ? dBear2 - 360 : dBear2;

                //*知识点*
                //计算两条直线（目标方位 - 相机指示线）相较于正北的角度差，假设为θ。
                //观察两条直线的位置关系，确定旋转的方向：
                //若θ为正值，表示直线顺时针旋转可以与另一条直线重合；
                //若θ为负值，表示直线逆时针旋转可以与另一条直线重合。

                //（相较于正北向）如果相机指示线与相机到目标连线的夹角大于1.5度，则需要转动相机
                //相机指示线比相机到目标连线的角度大，则相机需要往右转动;
                //...相机指示线比相机到目标连线的角度小，则相机需要往左转动
                if (Math.abs(dBear2 - dBear1) > 1.2) {
                    //顺时针
                    if (dBear2 > dBear1) {
                        traceProInfo.setRotaDir(1);
                    }
                    //逆时针
                    else {
                        traceProInfo.setRotaDir(0);
                    }
                    traceProInfo.setbStopRota(false);//开启转动
                    if (yzCameraInfo.getManu().equals("hik")) {

                    } else if (yzCameraInfo.getManu().equals("dh")) {

                    } else if (yzCameraInfo.getManu().equals("hp")) {
                        hpCameraProc.aziControl(yzCameraInfo, traceProInfo.getRotaDir(), false);
                    } else if (yzCameraInfo.getManu().equals("gpl")) {

                    }

                } else {
                    traceProInfo.setbStopRota(true);//停止转动
                    if (yzCameraInfo.getManu().equals("hik")) {

                    } else if (yzCameraInfo.getManu().equals("dh")) {

                    } else if (yzCameraInfo.getManu().equals("hp")) {
                        hpCameraProc.aziControl(yzCameraInfo, traceProInfo.getRotaDir(), true);
                    } else if (yzCameraInfo.getManu().equals("gpl")) {
                    }
                }
            }
        }
    }


    /**
     * 通过PT值控制相机
     *
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    private TraceProInfo ctrlCameraByPT(TraceProInfo traceProInfo) throws Exception {
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        //偏移校准值
        double pCorVal = yzCameraInfo.getAngle();
        double tCorVal = yzCameraInfo.getCurTVal();
        double zFixVal = yzCameraInfo.getCurZVal();
        double height = yzCameraInfo.getHeight();
        //相对于相机的角度
        double dBear = DisAndAngleUtils.get_bearing(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        //相对于相机的距离
        double dis = DisAndAngleUtils.get_distance(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;
        //TODO 计算出的T值
        double tVal = -1 * (90 - Math.atan(dis / height) * 180 / Math.PI);//旧的方法
//        double tVal = toDegrees(atan2(-1 * height, dis));
//        double tVal = -1 * toDegrees(asin(height / dis));

        if (yzCameraInfo.getManu().equals("hik")) {
//            hkCameraProc.ptzControl(yzCameraInfo, pVal, tVal, iZVal.doubleValue());
        } else if (yzCameraInfo.getManu().equals("dh")) {
//            dhCameraProc.ptzControl(yzCameraInfo, pVal, tVal, iZVal.doubleValue());
        } else if (yzCameraInfo.getManu().equals("hp")) {
            hpCameraProc.ptControl(yzCameraInfo, pVal, tVal);
        }
        return traceProInfo;
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
        double zFixVal = yzCameraInfo.getCurZVal();
        double height = yzCameraInfo.getHeight();
        //相对于相机的角度
        double dBear = DisAndAngleUtils.get_bearing(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        //相对于相机的距离
        double dis = DisAndAngleUtils.get_distance(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        double pVal = (dBear - pCorVal) > 360 ? dBear - pCorVal - 360 : dBear - pCorVal;
        //TODO 计算出的T值
        double tVal = -1 * (90 - Math.atan(dis / height) * 180 / Math.PI);
//        double tVal = toDegrees(atan2(-1 * height, dis));
//        double tVal = -1 * toDegrees(asin(height / dis));//旧的方法

        Integer iZVal = 30;

        if (yzCameraInfo.getManu().equals("hik")) {
            hkCameraProc.ptzControl(yzCameraInfo, pVal, tVal, iZVal.doubleValue());
        } else if (yzCameraInfo.getManu().equals("dh")) {
            dhCameraProc.ptzControl(yzCameraInfo, pVal, tVal, iZVal.doubleValue());
        } else if (yzCameraInfo.getManu().equals("hp")) {
            hpCameraProc.ptzControl(yzCameraInfo, pVal, tVal, iZVal.doubleValue());
        }
        return true;
    }

    /**
     * 海康图像跟踪
     *
     * @param traceProInfo
     * @param yzCameraInfo
     * @return
     */
    private boolean hikPhotoTrace(TraceProInfo traceProInfo, YzCameraInfo yzCameraInfo) {
        //相对于相机的角度
        double dBear = DisAndAngleUtils.get_bearing(yzCameraInfo.getLat().doubleValue(),
                yzCameraInfo.getLon().doubleValue(), traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        dBear = dBear < 0 ? 360 + dBear : dBear;
        double pVal = (dBear - yzCameraInfo.getpVal()) > 360 ? dBear - yzCameraInfo.getpVal() - 360 : dBear - yzCameraInfo.getpVal();
        Double f_Ele = yzCameraInfo.getCurTVal();
        Double f_Azi = (pVal - yzCameraInfo.getAngle() + 360) % 360;
        Double f_Dis = DisAndAngleUtils.get_distance(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        String szUrl = "PUT /ISAPI/PTZCtrl/channels/1/absoluteEx\r\n";
        String pBuf = String.format("<PTZAbsoluteEx version=\"2.0\" xmlns=\"http://www.hikvision.com/ver20/XMLSchema\">" +
                "<elevation>%f</elevation>" +
                "<azimuth>%f</azimuth>" +
                "<horizontalSpeed>1.00</horizontalSpeed>" +
                "<verticalSpeed>1.00</verticalSpeed>" +
                "<objectDistance>%f</objectDistance>" +
                "<isContinuousTrackingEnabled>true</isContinuousTrackingEnabled>" +
                "</PTZAbsoluteEx>", f_Ele, f_Azi, f_Dis);

        NET_DVR_XML_CONFIG_INPUT struInput = new NET_DVR_XML_CONFIG_INPUT();
        struInput.dwSize = struInput.size();
        struInput.lpRequestUrl = new Memory(szUrl.length() + 1);
        struInput.lpRequestUrl.setString(0, szUrl);
        struInput.dwRequestUrlLen = szUrl.length();
        struInput.dwRecvTimeOut = 5000;

        struInput.lpInBuffer = new Memory(pBuf.length() + 1);
        struInput.lpInBuffer.setString(0, pBuf);
        struInput.dwInBufferSize = pBuf.length();
        struInput.byForceEncrpt = 0;
        struInput.byNumOfMultiPart = 0;


        NET_DVR_XML_CONFIG_OUTPUT struOutput = new NET_DVR_XML_CONFIG_OUTPUT();
        struOutput.dwSize = struOutput.size();
        struOutput.lpOutBuffer = new Memory(10 * 1024);
        struOutput.dwOutBufferSize = 10 * 1024;
        struOutput.dwReturnedXMLSize = 0;
        struOutput.lpStatusBuffer = new Memory(1024);
        struOutput.dwStatusSize = 1024;

        boolean success = yzCameraInfo.getHkNetSDK().NET_DVR_STDXMLConfig(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())),
                struInput, struOutput);
        if (!success) {
            System.out.println("海康相机图像跟踪失败");
            return false;
        } else {
            return true;
        }
    }

    /**
     * 发送数据给MQ
     *
     * @param obj
     * @throws Exception
     */
    private void sendStatusToMq(Object obj) throws Exception {
        //发送状态信息给MQ
        mqConnectionUtil.getChannel().basicPublish(mqConnectionUtil.EXCHANGE_NAME, "camera_status.key",
                properties, new Gson().toJson(obj).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 海康相机PTZ值转换
     *
     * @param pPTZPos
     */
    private YzCameraInfo hikCon2Angle(YzCameraInfo yzCameraInfo, NET_DVR_PTZPOS pPTZPos) {
        yzCameraInfo.setOriPVal(Double.valueOf(String.format("%04x", pPTZPos.wPanPos)) / 10.0);
        Double dPVal = (Double.valueOf(String.format("%04x", pPTZPos.wPanPos)) / 10.0 + yzCameraInfo.getAngle() + 360.0) % 360.0;
        yzCameraInfo.setCurPVal(dPVal);
        Double tVal = Double.valueOf(String.format("%04x", pPTZPos.wTiltPos));
        DecimalFormat df = new DecimalFormat("#.#");
        String formattedNumber = df.format(tVal > 888 ? -1 * (360 - tVal / 10.0) : tVal / 10.0);
        yzCameraInfo.setCurTVal(Double.valueOf(formattedNumber));
        yzCameraInfo.setCurZVal(Double.valueOf(String.format("%04x", pPTZPos.wZoomPos)) / 10.0);
        return yzCameraInfo;
    }

    /**
     * 大华相机PTZ值转换
     *
     * @param pPTZPos
     */
    private YzCameraInfo dhCon2Angle(YzCameraInfo yzCameraInfo, DH_PTZ_LOCATION_INFO pPTZPos) {
        yzCameraInfo.setOriPVal(Double.valueOf(pPTZPos.nPTZPan) / 10.0);
        Double dPVal = (Double.valueOf(pPTZPos.nPTZPan) / 10.0 + yzCameraInfo.getAngle() + 360.0) % 360.0;
        yzCameraInfo.setCurPVal(dPVal);
        Double tVal = Double.valueOf(pPTZPos.nPTZTilt) / 10.0;
        DecimalFormat df = new DecimalFormat("#.#");
//        String formattedNumber = df.format(tVal > 888 ? -1 * (360 - tVal / 10.0) : tVal / 10.0);
        yzCameraInfo.setCurTVal(tVal);
        yzCameraInfo.setCurZVal(Double.valueOf(pPTZPos.nPTZZoom) / 100.0);
        return yzCameraInfo;
    }


    /**
     * 高普乐相机PTZ值转换
     *
     * @param pPTZPos
     */
    private YzCameraInfo gplCon2Angle(YzCameraInfo yzCameraInfo, VS_PTZ_LOCATION_INFO pPTZPos) {
        yzCameraInfo.setOriPVal(Double.valueOf(pPTZPos.nPTZPan) / 10.0);
        Double dPVal = (Double.valueOf(pPTZPos.nPTZPan) / 10.0 + yzCameraInfo.getAngle() + 360.0) % 360.0;
        yzCameraInfo.setCurPVal(dPVal);
        Double tVal = Double.valueOf(pPTZPos.nPTZTilt) / 10.0;
//        DecimalFormat df = new DecimalFormat("#.#");
//        String formattedNumber = df.format(tVal > 888 ? -1 * (360 - tVal / 10.0) : tVal / 10.0);
        yzCameraInfo.setCurTVal(tVal);
        yzCameraInfo.setCurZVal(Double.valueOf(pPTZPos.nPTZZoom) / 100.0);
        return yzCameraInfo;
    }


    /**
     * 1000ms发送一次查看强光是否开启指令
     */
    @Scheduled(fixedDelay = 1000)
    public void cronQueryLightIsOpen() {
        //强光状态查询指令
        byte[] lightStatusQueryOrder = {(byte) 0xA7, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x07, (byte) 0x02, (byte) 0x00, (byte) 0x0C};

        for (Map.Entry<Long, YzCameraInfo> entry : GL_CameraInfoMap.entrySet()) {
            YzCameraInfo yzCameraInfo = entry.getValue();

            GplCmdSender commandSender = new GplCmdSender();
            commandSender.sendCommand(yzCameraInfo, lightStatusQueryOrder);
        }
    }
}
