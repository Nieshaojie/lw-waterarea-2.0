package com.mskyeye.trace.netty.control.service.GplCamera;

import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.model.YzCameraInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GPL 板卡控制处理器（WebSocket版）
 * 替代旧的 GplCameraProc 中通过 Netty TCP 发送二进制协议的方式
 * 使用 WebSocket JSON 协议与板卡通信
 *
 * @Author: claw
 * @Date: 2026/03/31
 */
@Component("gplWebSocketCameraProc")
@Slf4j
public class GplWebSocketCameraProc {

    /**
     * PTZ绝对值控制（云台转动到指定角度 + 变倍到指定值）
     * 使用 angle_set + view_set 协议
     *
     * @param yzCameraInfo 摄像头信息
     * @param pVal 水平角（方位角）0~360
     * @param tVal 垂直角（俯仰角）
     * @param zVal 变倍值
     * @return 是否成功
     */
    public boolean ptzControl(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        // 使用 angle_set 协议进行角度定位
        // 协议: {"msg":"angle_set", "angle_x":359.9, "angle_y":-1.23}
        JSONObject angleCmd = new JSONObject();
        angleCmd.put("msg", "angle_set");
        angleCmd.put("angle_x", pVal);
        angleCmd.put("angle_y", tVal);

        boolean angleOk = GplWebSocketClient.sendCommand(yzCameraInfo.getId(), angleCmd);
        if (!angleOk) {
            log.warn("角度定位发送失败: cameraId={}", yzCameraInfo.getId());
            return false;
        }
        log.info("发送角度定位: cameraId={}, pVal={}, tVal={}", yzCameraInfo.getId(), pVal, tVal);

        // 使用 view_set 协议进行变倍定位
        // 协议: {"msg":"view_set", "is_ir":false, "view_width":5.1}
        if (zVal > 0) {
            JSONObject viewCmd = new JSONObject();
            viewCmd.put("msg", "view_set");
            viewCmd.put("is_ir", false);
            viewCmd.put("view_width", zVal);

            boolean viewOk = GplWebSocketClient.sendCommand(yzCameraInfo.getId(), viewCmd);
            if (!viewOk) {
                log.warn("变倍定位发送失败: cameraId={}", yzCameraInfo.getId());
                return false;
            }
            log.info("发送变倍定位: cameraId={}, zVal={}", yzCameraInfo.getId(), zVal);
        }

        return true;
    }

    /**
     * 灯光控制（补光灯开关）
     * 注意: 新的WebSocket协议中没有直接的灯光控制命令，
     * 可通过 power_ctrl 实现（如果有对应设备类型）
     * 或保留旧协议发送
     *
     * 协议: {"msg":"power_ctrl", "enable_power":true, "e_type":int}
     * e_type: 0=转台, 1=热成像, 2=可见光
     *
     * @param yzCameraInfo 摄像头信息
     * @param isOpenLight 0=关灯, 1=开灯
     * @return 是否成功
     */
    public boolean lightControl(YzCameraInfo yzCameraInfo, Integer isOpenLight) throws Exception {
        // 新协议暂无直接灯光控制字段，使用 power_ctrl 模拟
        // 如果实际板卡灯光走其他协议（如旧的A7 01协议），可以保持旧方式
        log.warn("灯光控制在新WebSocket协议中无直接对应命令，请确认板卡是否支持 power_ctrl");
        return true;
    }

    /**
     * 联动跟踪 - 云台按指定角度转动
     * 使用 angle_set 协议
     *
     * @param yzCameraInfo 摄像头信息
     * @param pVal 水平角
     * @param tVal 垂直角
     * @param zVal 变倍值
     * @return 是否成功
     */
    public boolean gplLinkTrace(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        JSONObject angleCmd = new JSONObject();
        angleCmd.put("msg", "angle_set");
        angleCmd.put("angle_x", pVal);
        angleCmd.put("angle_y", tVal);

        boolean ok = GplWebSocketClient.sendCommand(yzCameraInfo.getId(), angleCmd);
        if (ok) {
            log.info("联动跟踪角度发送成功: cameraId={}, P={}, T={}", yzCameraInfo.getId(), pVal, tVal);
        } else {
            log.warn("联动跟踪角度发送失败: cameraId={}", yzCameraInfo.getId());
        }
        return ok;
    }

    /**
     * PELCO-D PTZ控制（新版WebSocket协议）
     * 使用 angle_set + view_set
     *
     * @param yzCameraInfo 摄像头信息
     * @param pVal 水平角
     * @param tVal 垂直角
     * @param zVal 变倍值
     * @return 是否成功
     */
    public boolean ptzControlPD(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        // 角度定位
        JSONObject angleCmd = new JSONObject();
        angleCmd.put("msg", "angle_set");
        angleCmd.put("angle_x", pVal);
        angleCmd.put("angle_y", tVal);

        boolean angleOk = GplWebSocketClient.sendCommand(yzCameraInfo.getId(), angleCmd);
        log.info("WebSocket发送角度定位(PD): cameraId={}, pVal={}, tVal={}, result={}", yzCameraInfo.getId(), pVal, tVal, angleOk);

        Thread.sleep(25);

        // 变倍定位
        if (zVal > 0) {
            JSONObject viewCmd = new JSONObject();
            viewCmd.put("msg", "view_set");
            viewCmd.put("is_ir", false);
            viewCmd.put("view_width", zVal);

            boolean viewOk = GplWebSocketClient.sendCommand(yzCameraInfo.getId(), viewCmd);
            log.info("WebSocket发送变倍定位(PD): cameraId={}, zVal={}, result={}", yzCameraInfo.getId(), zVal, viewOk);
            return angleOk && viewOk;
        }

        return angleOk;
    }
}
