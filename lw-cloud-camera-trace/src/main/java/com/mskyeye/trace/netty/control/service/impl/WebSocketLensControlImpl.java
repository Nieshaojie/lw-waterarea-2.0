package com.mskyeye.trace.netty.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.service.CameraLensControl;
import com.mskyeye.trace.netty.control.service.GplCamera.GplWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于 WebSocket JSON 协议的镜头/云台控制实现
 * 对接 GPL 板卡 WebSocket 协议文档 7.2 设备实时控制
 *
 * 协议 MsgType 枚举:
 *   VIEW_ADD=0(变倍+), VIEW_SUB=1(变倍-), VIEW_STOP=2(变倍停),
 *   FOCUS_ADD=3(聚焦+), FOCUS_SUB=4(聚焦-), FOCUS_STOP=5(聚焦停), FOCUS_AUTO=6(自动聚焦),
 *   HEAD_LEFT=7, HEAD_RIGHT=8, HEAD_UP=9, HEAD_DOWN=10,
 *   HEAD_LEFT_UP=11, HEAD_RIGHT_UP=13, HEAD_LEFT_DOWN=12, HEAD_RIGHT_DOWN=14,
 *   HEAD_STOP=15(云台全停),
 *   WIPER_CTRL=16, IR_IMG_CORRECTION=17
 *
 * @Author: claw
 * @Date: 2026/03/31
 */
@Slf4j
@Component("webSocketLensControl")
public class WebSocketLensControlImpl implements CameraLensControl {

    /** 默认控制速度(%) */
    private static final int DEFAULT_SPEED = 5;

    /**
     * type="1" -> 可见光 (is_ir=false)
     * type="2" -> 热成像 (is_ir=true)
     */
    private boolean parseIsIr(String type) {
        return "2".equals(type);
    }

    /**
     * 构建 ptz_ctrl JSON 命令
     * 协议格式: {"msg":"ptz_ctrl", "is_ir":true/false, "action":int, "value":int}
     */
    private JSONObject buildPtzCmd(boolean isIr, int action, int value) {
        JSONObject json = new JSONObject();
        json.put("msg", "ptz_ctrl");
        json.put("is_ir", isIr);
        json.put("action", action);
        json.put("value", value);
        return json;
    }

    /**
     * 发送命令
     */
    private void send(YzCameraInfo camera, JSONObject json, String actionDesc) throws Exception {
        boolean ok = GplWebSocketClient.sendCommand(camera.getId(), json);
        if (!ok) {
            throw new RuntimeException("WebSocket发送失败: " + actionDesc);
        }
        log.info("[{}] cameraId={}, msg={}", actionDesc, camera.getId(), json.toJSONString());
    }

    // ==================== 镜头控制 ====================

    @Override
    public void zoomIn(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 0, DEFAULT_SPEED), "变倍+");
    }

    @Override
    public void zoomOut(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 1, DEFAULT_SPEED), "变倍-");
    }

    @Override
    public void focusNear(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 3, DEFAULT_SPEED), "聚焦+");
    }

    @Override
    public void focusFar(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 4, DEFAULT_SPEED), "聚焦-");
    }

    @Override
    public void irisClose(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 0, DEFAULT_SPEED), "光圈+");
    }

    @Override
    public void irisOpen(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 1, DEFAULT_SPEED), "光圈-");
    }

    @Override
    public void stop(YzCameraInfo camera, String type) throws Exception {
        // 发送变倍停 + 聚焦停 (对应 VIEW_STOP=2, FOCUS_STOP=5)
        send(camera, buildPtzCmd(parseIsIr(type), 2, 0), "变倍停");
        send(camera, buildPtzCmd(parseIsIr(type), 5, 0), "聚焦停");
    }

    // ==================== AI自动跟踪控制 ====================

    /**
     * 开启 AI 自动跟踪
     * 协议: {"msg":"tracker_ctrl", "is_start":true, "is_ir":false, "e_type":1, "left":0, "top":0, "right":0, "bottom":0}
     */
    @Override
    public void startAiTrack(YzCameraInfo camera) throws Exception {
        JSONObject json = new JSONObject();
        json.put("msg", "tracker_ctrl");
        json.put("is_start", true);
        json.put("is_ir", false);
        json.put("e_type", 1);  // 自动跟踪
        json.put("left", 0);
        json.put("top", 0);
        json.put("right", 0);
        json.put("bottom", 0);
        send(camera, json, "AI自动跟踪-开启");
    }

    /**
     * 关闭 AI 自动跟踪
     * 协议: {"msg":"tracker_ctrl", "is_start":false, "is_ir":false, "e_type":1, "left":0, "top":0, "right":0, "bottom":0}
     */
    @Override
    public void stopAiTrack(YzCameraInfo camera) throws Exception {
        JSONObject json = new JSONObject();
        json.put("msg", "tracker_ctrl");
        json.put("is_start", false);
        json.put("is_ir", false);
        json.put("e_type", 1);
        json.put("left", 0);
        json.put("top", 0);
        json.put("right", 0);
        json.put("bottom", 0);
        send(camera, json, "AI自动跟踪-关闭");
    }

    // ==================== 云台控制 ====================

    /**
     * 云台方向控制
     * @param camera   摄像头信息
     * @param type     通道类型: "1"=可见光, "2"=热成像
     * @param direction 方向: UP, DOWN, LEFT, RIGHT, LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN
     * @param speed    速度: 1~100
     */
    @Override
    public void controlPTZ(YzCameraInfo camera, String type, String direction, int speed) throws Exception {
        int action;
        switch (direction.toUpperCase()) {
            case "UP":          action = 9;  break;  // HEAD_UP
            case "DOWN":        action = 10; break;  // HEAD_DOWN
            case "LEFT":        action = 7;  break;  // HEAD_LEFT
            case "RIGHT":       action = 8;  break;  // HEAD_RIGHT
            case "LEFT_UP":     action = 11; break;  // HEAD_LEFT_UP
            case "RIGHT_UP":    action = 13; break;  // HEAD_RIGHT_UP
            case "LEFT_DOWN":   action = 12; break;  // HEAD_LEFT_DOWN
            case "RIGHT_DOWN":  action = 14; break;  // HEAD_RIGHT_DOWN
            default:
                log.warn("未知云台方向: {}", direction);
                return;
        }
        // speed: 1~100 -> 协议 value: 1~100 (%)
        send(camera, buildPtzCmd(parseIsIr(type), action, speed), "云台控制(" + direction + ")");
    }

    /**
     * 云台全停
     */
    @Override
    public void stopPTZ(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildPtzCmd(parseIsIr(type), 15, 0), "云台全停");
    }
}
