package com.mskyeye.trace.proc;


import com.alibaba.fastjson2.JSONObject;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.utils.MD5SaltUtil;
import com.mskyeye.trace.utils.PostRequestUtil;
import com.mskyeye.trace.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

/**
 * @ClassName:HpCameraProc
 * @Description:和普相机功能类
 * @Author:R.Gong
 * @Date:2023/8/1 15:28
 * @Version:1.0
 **/
@Component
public class HpCameraProc {
    private static final Logger log = LoggerFactory.getLogger(HpCameraProc.class);

    /**
     * Salt获取
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public String userSaltGet(YzCameraInfo yzCameraInfo) throws Exception {
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonBody1 = new JSONObject();
        jsonBody.put("cmd", "userSaltGet");
        jsonBody1.put("username", yzCameraInfo.getUserName());
        jsonBody.put("param", jsonBody1);
        String body = jsonBody.toJSONString();
        String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
        TimeUnit.MILLISECONDS.sleep(200);
        if (StringUtil.isNotEmpty(result) && result.contains("salt")) {
            JSONObject resJson = JSONObject.parseObject(result);
            return (String) ((JSONObject) resJson.get("param")).get("salt");
        } else {
//            System.out.println("************和普接口userSaltGet报错*****相机名称为:" + yzCameraInfo.getName());
            return null;
        }
    }

    /**
     * 用户登录
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public String userLogin(YzCameraInfo yzCameraInfo) throws Exception {
        String salt = userSaltGet(yzCameraInfo);
        if (salt == null) {
            return null;
        }
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonBody1 = new JSONObject();
        jsonBody.put("cmd", "userLogin");
        jsonBody1.put("username", yzCameraInfo.getUserName());
        jsonBody1.put("password", MD5SaltUtil.encrypt(yzCameraInfo.getPassWord(), salt));
        jsonBody.put("param", jsonBody1);
        String body = jsonBody.toJSONString();
        String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
        TimeUnit.MILLISECONDS.sleep(200);
        if (StringUtil.isNotEmpty(result) && result.contains("token")) {
            JSONObject resJson = JSONObject.parseObject(result);
            return (String) ((JSONObject) resJson.get("param")).get("token");
        } else {
//            System.out.println("************和普接口userLogin报错");
            return null;
        }
    }

    /**
     * 用户注销
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public boolean userLogout(YzCameraInfo yzCameraInfo) throws Exception {
        JSONObject jsonBody = new JSONObject();
        JSONObject jsonBody1 = new JSONObject();
        jsonBody.put("cmd", "userLogout");
        jsonBody1.put("token", yzCameraInfo.getLoginInfo());
        jsonBody.put("param", jsonBody1);
        String body = jsonBody.toJSONString();
        PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
        TimeUnit.MILLISECONDS.sleep(200);
        return true;
    }


    /**
     * 获取设备方位、俯仰信息(P、T值)
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public JSONObject ptInfoGet(YzCameraInfo yzCameraInfo) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ptzAngleInfoGet");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            if (StringUtil.isNotEmpty(result) && result.contains("angleInfoCurHor")) {
                return JSONObject.parseObject(result);
            } else {
//                System.out.println("************和普接口ptzAngleInfoGet报错");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidJson(String jsonString) {
        try {
            JSONObject.parseObject(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取视场角度(Z值)
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public JSONObject visibleGetFocalInfo(YzCameraInfo yzCameraInfo) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "visibleGetFocalInfo");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
            if (StringUtil.isNotEmpty(result) && result.contains("camFocalCurFocal")) {
                return JSONObject.parseObject(result);
            } else {
//                System.out.println("************和普接口visibleGetFocalInfo报错");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 在线用户心跳
     *
     * @param yzCameraInfo
     * @return
     * @throws Exception
     */
    public void userOnlineHeart(YzCameraInfo yzCameraInfo) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "userOnlineHeart");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.  sleep(200);
            if (StringUtil.isNotEmpty(result) && result.contains("ackvalue")) {
//                System.out.println("心跳发送成功，返回："+result.toString()+"    相机信息："+yzCameraInfo.toString());
            } else {
//                System.out.println("************和普接口userOnlineHeart报错*****相机名称为:" + yzCameraInfo.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 控制PTZ
     *
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @param zVal
     * @return
     * @throws Exception
     */
    public boolean ptzControl(YzCameraInfo yzCameraInfo, Double pVal, Double tVal, Double zVal) throws Exception {
        try {
            Integer iPVal = (int) (pVal * 100);

            Integer iTVal = (int) (tVal * 100);


            if (iTVal < 0) {
                iTVal = -1 * iTVal;
            } else {
                iTVal = 36000 - iTVal;
            }

            //修改PT值
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ptzControl");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", 0);
            jsonBody1.put("actionid", 51);
            jsonBody1.put("locSpeed", 200);
            jsonBody1.put("locYSpeed", 200);
            jsonBody1.put("bptzSpeedAb", 0);
            jsonBody1.put("locHorPos", iPVal);
            jsonBody1.put("locVerPos", iTVal);
            jsonBody1.put("ptzPosCamview", 0);
            jsonBody1.put("ptzPosIrview", 0);
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
            jsonBody = new JSONObject();
            jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ptzControl");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", 0);
            jsonBody1.put("actionid", 44);
            jsonBody1.put("locLensFocalPos", zVal.intValue());
            jsonBody.put("param", jsonBody1);
            body = jsonBody.toJSONString();
            PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 控制PT
     *
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @return
     * @throws Exception
     */
    public boolean ptControl(YzCameraInfo yzCameraInfo, Double pVal, Double tVal) throws Exception {
        try {
            Integer iPVal = (int) (pVal * 100);

            Integer iTVal = (int) (tVal * 100);


            if (iTVal < 0) {
                iTVal = -1 * iTVal;
            } else {
                iTVal = 36000 - iTVal;
            }

            //修改PT值
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ptzControl");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", 0);
            jsonBody1.put("actionid", 51);
            jsonBody1.put("locSpeed", 500);
            jsonBody1.put("locYSpeed", 1);
            jsonBody1.put("bptzSpeedAb", 1);
            jsonBody1.put("locHorPos", iPVal);
            jsonBody1.put("locVerPos", iTVal);
            jsonBody1.put("ptzPosCamview", 0);
            jsonBody1.put("ptzPosIrview", 0);
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 相机方位控制
     * 转动方向rotaDir：0 左 1 右
     *
     * @param yzCameraInfo
     * @param rotaDir
     * @return
     * @throws Exception
     */
    public boolean aziControl(YzCameraInfo yzCameraInfo, Integer rotaDir, boolean bStop) {
        try {
            Integer actionId = 8;//默认为停止
            if (!bStop) {
                actionId = rotaDir == 0 ? 2 : 3;
            }
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ptzControl");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", 0);
            jsonBody1.put("actionid", actionId);
            if (!bStop) {
                jsonBody1.put("bptzSpeedAb", 0);
                jsonBody1.put("ptzxspeed", 2);
                jsonBody1.put("ptAutoStopTime", 0);
            }
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 框选跟踪
     *
     * @param yzCameraInfo
     * @param bTracking
     * @param channelid
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @throws Exception
     */
    public void boxTrackCtrl(YzCameraInfo yzCameraInfo,
                             Boolean bTracking, Integer channelid,
                             Integer x, Integer y,
                             Integer w, Integer h) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            JSONObject jsonBody2 = new JSONObject();
            jsonBody2.put("x", x);
            jsonBody2.put("y", y);
            jsonBody2.put("w", w);
            jsonBody2.put("h", h);
            jsonBody1.put("trackingRect", jsonBody2);
            jsonBody.put("cmd", "ivpTrackingCtrl");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("bTracking", bTracking);
            jsonBody1.put("channelid", channelid);
            jsonBody1.put("trackingTime", 180);//跟踪时长，单位秒
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开启图像跟踪
     *
     * @param yzCameraInfo
     * @param enable
     * @param channelid
     * @return
     * @throws Exception
     */
    public void photoTrackingCtrl(YzCameraInfo yzCameraInfo, Boolean enable, Integer channelid) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "ivpSet");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", channelid);
            jsonBody1.put("enable", /*enable?1:0*/0);
            jsonBody1.put("bAlarmTracking", enable);
            jsonBody1.put("type", 10);
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新登录和普相机(用于发送错误的时候)
     *
     * @param yzCameraInfo
     * @return
     */
    public void reLoginHpCamera(YzCameraInfo yzCameraInfo) throws Exception {
        String token = userLogin(yzCameraInfo);
//        if(StringUtil.isEmpty(token)){
//            return;
//        }
        yzCameraInfo.setLoginInfo(token);
        if (GL_CameraInfoMap.containsKey(yzCameraInfo.getId())) {
            YzCameraInfo oldYzCameraInfo = GL_CameraInfoMap.get(yzCameraInfo.getId());
            yzCameraInfo.setCurPVal(oldYzCameraInfo.getCurPVal());
            yzCameraInfo.setCurTVal(oldYzCameraInfo.getCurTVal());
            yzCameraInfo.setCurZVal(oldYzCameraInfo.getCurZVal());
            yzCameraInfo.setAngle(oldYzCameraInfo.getAngle());
            yzCameraInfo.setpVal(oldYzCameraInfo.getpVal());
            yzCameraInfo.settVal(oldYzCameraInfo.gettVal());
            yzCameraInfo.setzVal(oldYzCameraInfo.getzVal());
        }
        GL_CameraInfoMap.put(yzCameraInfo.getId(), yzCameraInfo);
    }

    /**
     * 目标位置经纬高坐标引导
     *
     * @param yzCameraInfo
     * @param traceProInfo
     * @return
     * @throws Exception
     */
    public void trackingCtrl(YzCameraInfo yzCameraInfo, TraceProInfo traceProInfo) throws Exception {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonBody1 = new JSONObject();
            jsonBody.put("cmd", "deviceTargetLBHGuide");
            jsonBody1.put("token", yzCameraInfo.getLoginInfo());
            jsonBody1.put("channelid", traceProInfo.getCameraId());
            jsonBody1.put("localNumber", 50);
            jsonBody1.put("targetPosL", traceProInfo.getTraceLat()*10000000);
            jsonBody1.put("targetPosB", traceProInfo.getTraceLon()*10000000);
            jsonBody1.put("targetPosH", traceProInfo.getTraceLon()*100);
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            String result = PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);

            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
