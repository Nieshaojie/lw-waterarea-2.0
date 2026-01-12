package com.mskyeye.trace.proc;


import com.alibaba.fastjson2.JSONObject;
import com.mskyeye.trace.enums.PtzDirection;
import com.mskyeye.trace.enums.PtzFocusAction;
import com.mskyeye.trace.enums.PtzSpeedMapper;
import com.mskyeye.trace.enums.PtzZoomAction;
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


    /**
     * 云台方向控制统一入口
     *
     * 能力：
     * 1. 支持 8 个方向 + STOP
     * 2. STOP 是显式命令
     * 3. 支持自动停止（避免云台失控）
     * 4. 前端速度等级与设备速度解耦
     *
     * @param yzCameraInfo 相机登录信息
     * @param direction   云台方向（含 STOP）
     * @param speedLevel  前端速度等级（0-100，STOP 时忽略）
     */
    public boolean ptzDirectionControl(YzCameraInfo yzCameraInfo,
                                       PtzDirection direction,
                                       int speedLevel) {
        try {
            JSONObject param = new JSONObject();
            param.put("token", yzCameraInfo.getLoginInfo());
            param.put("channelid", 0);
            param.put("actionid", direction.getActionId());

            // ====== 方向需要速度时才下发 ======
            if (direction.needSpeed()) {
                int deviceSpeed = PtzSpeedMapper.toDeviceSpeed(speedLevel);

                // 有些设备需要声明速度模式
                param.put("bptzSpeedAb", 0);

                // 单轴 or 双轴速度
                for (String field : direction.getSpeedFields()) {
                    param.put(field, deviceSpeed);
                }

            }

            JSONObject body = new JSONObject();
            body.put("cmd", "ptzControl");
            body.put("param", param);

            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body.toJSONString()
            );
            return true;
        } catch (Exception e) {
            log.error("ptzDirectionControl failed, dir={}", direction, e);
            return false;
        }
    }

    /**
     * 镜头变倍（Zoom）控制
     *
     * 能力：
     * 1. 支持 放大 / 缩小 / 停止
     * 2. 前端速度等级与设备真实速度解耦
     * 3. 支持自动停止，防止镜头长时间运动
     *
     * @param yzCameraInfo 相机信息
     * @param action       镜头动作（ZOOM_IN / ZOOM_OUT / ZOOM_STOP）
     * @param speedLevel   前端速度等级（0-100，STOP 时忽略）
     * @param type   相机通道 0：可见光 1：热像
     */
    public boolean ptzZoomControl(YzCameraInfo yzCameraInfo,
                                  PtzZoomAction action,
                                  int speedLevel,int type) {
        try {
            JSONObject param = new JSONObject();
            param.put("token", yzCameraInfo.getLoginInfo());
            param.put("channelid", type);
            param.put("actionid", action.getActionId());

            // ====== 只有需要速度的动作才下发速度 ======
            if (action.needSpeed()) {
                int deviceSpeed = PtzSpeedMapper.toDeviceSpeed(speedLevel);

                // 镜头速度字段（设备定义）
                param.put("ptzzoomspeed", deviceSpeed);

            }

            JSONObject body = new JSONObject();
            body.put("cmd", "ptzControl");
            body.put("param", param);

            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body.toJSONString()
            );
            return true;
        } catch (Exception e) {
            log.error("ptzZoomControl failed, action={}", action, e);
            return false;
        }
    }

    /**
     * 镜头聚焦（Focus）控制
     *
     * 特点：
     * 1. 行为模型与方向 / Zoom 完全一致
     * 2. 支持自动停止，防止“对焦打满”
     * 3. STOP 是明确命令
     *
     * @param yzCameraInfo 相机信息
     * @param action       聚焦动作（NEAR / FAR / STOP）
     * @param speedLevel   前端速度等级（0-100，STOP 忽略）
     */
    public boolean ptzFocusControl(YzCameraInfo yzCameraInfo,
                                   PtzFocusAction action,
                                   int speedLevel,int type) {
        try {
            JSONObject param = new JSONObject();
            param.put("token", yzCameraInfo.getLoginInfo());
            param.put("channelid", type);
            param.put("actionid", action.getActionId());

            if (action.needSpeed()) {
                int deviceSpeed = PtzSpeedMapper.toDeviceSpeed(speedLevel);

                // 聚焦速度字段（设备定义）
                param.put("ptzfocuspeed", deviceSpeed);

            }

            JSONObject body = new JSONObject();
            body.put("cmd", "ptzControl");
            body.put("param", param);

            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body.toJSONString()
            );
            return true;
        } catch (Exception e) {
            log.error("ptzFocusControl failed, action={}", action, e);
            return false;
        }
    }


    /**
     * 设置云台水平速度（1-63）
     */
    public boolean setPtzSpeedX(YzCameraInfo yzCameraInfo, int speedX) {
        try {
            JSONObject param = new JSONObject();
            param.put("token", yzCameraInfo.getLoginInfo());
            param.put("ptzspeedX", speedX);

            JSONObject body = new JSONObject();
            body.put("cmd", "ptzWebSetPtzSpeedX");
            body.put("param", param);

            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body.toJSONString()
            );
            return true;
        } catch (Exception e) {
            log.error("setPtzSpeedX failed", e);
            return false;
        }
    }

    /**
     * 设置云台俯仰速度（1-63）
     */
    public boolean setPtzSpeedY(YzCameraInfo yzCameraInfo, int speedY) {
        try {
            JSONObject param = new JSONObject();
            param.put("token", yzCameraInfo.getLoginInfo());
            param.put("ptzspeedY", speedY);

            JSONObject body = new JSONObject();
            body.put("cmd", "ptzWebSetPtzSpeedY");
            body.put("param", param);

            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body.toJSONString()
            );
            return true;
        } catch (Exception e) {
            log.error("setPtzSpeedY failed", e);
            return false;
        }
    }

    /**
     * 控制PTZ
     *
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @param vVal1
     * @return
     * @throws Exception
     */
    public boolean ptvControl(YzCameraInfo yzCameraInfo, Double pVal, Double tVal, Double vVal1,Double vVal2) throws Exception {
        try {
            Integer iPVal = (int) (pVal * 100);

            Integer iTVal = (int) (tVal * 100);

            Integer ivVal1 = (int) (vVal1 * 100);

            Integer ivVal2 = (int) (vVal2 * 100);
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
            jsonBody1.put("ptzPosCamview", ivVal1);
            jsonBody1.put("ptzPosIrview", ivVal2);
            jsonBody.put("param", jsonBody1);
            String body = jsonBody.toJSONString();
            PostRequestUtil.sendToHpPostReq(yzCameraInfo.getIp(), String.valueOf(yzCameraInfo.getHttpPort()), body);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 识别联动自动跟踪开关
     */
    public void alarmAutoTrackingCtrl(YzCameraInfo yzCameraInfo,
                                      Boolean enable,
                                      Integer channelid) throws Exception {
        try {
            JSONObject bodyJson = new JSONObject();
            JSONObject paramJson = new JSONObject();

            bodyJson.put("cmd", "ivpSet");

            paramJson.put("token", yzCameraInfo.getLoginInfo());
            paramJson.put("channelid", channelid);
            paramJson.put("type", 10); // 目标分类
            paramJson.put("bAlarmTracking", enable);//开启自动跟踪
            paramJson.put("bAlarmTrackingAutoZoom", enable);//自动变倍
            bodyJson.put("param", paramJson);

            String body = bodyJson.toJSONString();
            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body
            );

            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 停止图像跟踪
     */
    public void stopPhotoTracking(YzCameraInfo yzCameraInfo,
                                  Integer channelid) throws Exception {
        try {
            JSONObject bodyJson = new JSONObject();
            JSONObject paramJson = new JSONObject();

            bodyJson.put("cmd", "ivpTrackingCtrl");

            paramJson.put("token", yzCameraInfo.getLoginInfo());
            paramJson.put("channelid", channelid);
            paramJson.put("bTracking", false);

            bodyJson.put("param", paramJson);

            String body = bodyJson.toJSONString();
            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body
            );

            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 自动跟踪变倍控制
     *
     * @param targetPixel 目标像素大小（1~600，1080P）
     */
    public void trackingAutoZoomCtrl(YzCameraInfo yzCameraInfo,
                                     Boolean enable,
                                     Integer targetPixel,
                                     Integer channelid) throws Exception {
        try {
            JSONObject bodyJson = new JSONObject();
            JSONObject paramJson = new JSONObject();

            bodyJson.put("cmd", "ivpSet");

            paramJson.put("token", yzCameraInfo.getLoginInfo());
            paramJson.put("channelid", channelid);
            paramJson.put("type", 10);
            paramJson.put("bAlarmTrackingAutoZoom", enable);

            if (enable && targetPixel != null) {
                paramJson.put("trackingAutoZoomCoef", targetPixel);
            }

            bodyJson.put("param", paramJson);

            String body = bodyJson.toJSONString();
            PostRequestUtil.sendToHpPostReq(
                    yzCameraInfo.getIp(),
                    String.valueOf(yzCameraInfo.getHttpPort()),
                    body
            );

            TimeUnit.MILLISECONDS.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
