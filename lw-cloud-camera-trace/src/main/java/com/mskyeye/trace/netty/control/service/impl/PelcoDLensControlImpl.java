package com.mskyeye.trace.netty.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.service.CameraLensControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 基于 VISCA JSON 透传（EC 91 03 封装）
 * 按 4.2.2 节要求：
 * - ControlType 固定为 SerialControl
 * - SerialType 固定为 PELCO_D 或 VISCA（Tofu设备仍透传PELCO_D结构）
 * - 可见光使用 ID=0x01
 * - 热像使用 ID=0x02（但协议依旧是 VISCA → 实际PELCO_D透传）
 */
@Slf4j
@Component
public class PelcoDLensControlImpl implements CameraLensControl {

    /** 构建 VISCA/PELCO_D 透传 JSON */
    private String buildViscaJson(String type, String hexCmd) {
        JSONObject json = new JSONObject();
        json.put("ControlType", "SerialControl");
        json.put("SerialType", "PELCO_D");

        JSONObject serialData = new JSONObject();
        serialData.put("Lens", 7);
        serialData.put("Data", hexCmd);
        json.put("SerialData", serialData);

        return json.toJSONString();
    }

    /** EC 91 03 封装（大端长度） */
    private byte[] wrapFrame(String json) {
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        int len = jsonBytes.length;

        ByteBuffer buf = ByteBuffer.allocate(3 + 4 + len);
        buf.put((byte) 0xEC);
        buf.put((byte) 0x91);
        buf.put((byte) 0x03);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(len);
        buf.put(jsonBytes);
        return buf.array();
    }

    private String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    /** 发送命令 */
    private void send(YzCameraInfo camera, String jsonStr, String action, String type) throws Exception {
        byte[] frame = wrapFrame(jsonStr);
        String channel = "1".equals(type) ? "可见光(VISCA,ID=0x01)" : "热像(VISCA→PELCO_D,ID=0x02)";
        log.info("发送命令 [{} - {}]: {}", channel, action, jsonStr);
        log.info("封装帧 HEX [{} - {}]: {}", channel, action, toHex(frame));
        camera.getGplAiCtrlTcpClient().sendInfo(frame);
    }

    /** 替换 ID（第2字节） */
    private String replaceId(String hexCmd, String type) {
        String id = "1".equals(type) ? "01" : "02";
        return "FF" + id + hexCmd.substring(4);
    }

    /*===================== 实现接口 =====================*/

    /** 镜头变倍+（已验证有效） */
    @Override
    public void zoomIn(YzCameraInfo camera, String type) throws Exception {
        send(camera, buildViscaJson(type, replaceId("FF010020040025", type)), "镜头变倍+", type);
    }

    /** 镜头变倍- */
    @Override
    public void zoomOut(YzCameraInfo camera, String type) throws Exception {
        // 对应 VISCA 变倍 OUT 命令： FF 01 00 40 00 00 41
        send(camera, buildViscaJson(type, replaceId("FF010040040045", type)), "镜头变倍-", type);
    }

    /** 聚焦+（Near） */
    @Override
    public void focusNear(YzCameraInfo camera, String type) throws Exception {
        // VISCA 聚焦+：FF 01 00 80 00 00 81
        send(camera, buildViscaJson(type, replaceId("FF010080000081", type)), "镜头聚焦+", type);
    }

    /** 聚焦-（Far） */
    @Override
    public void focusFar(YzCameraInfo camera, String type) throws Exception {
        // VISCA 聚焦-：FF 01 00 81 00 00 82 或 010100000002，根据协议选用后一种更常见
        send(camera, buildViscaJson(type, replaceId("FF010100000002", type)), "镜头聚焦-", type);
    }

    /** 光圈+ */
    @Override
    public void irisClose(YzCameraInfo camera, String type) throws Exception {
        // VISCA 光圈+：FF 01 04 00 00 00 05
        send(camera, buildViscaJson(type, replaceId("FF010400000005", type)), "镜头光圈+", type);
    }

    /** 光圈- */
    @Override
    public void irisOpen(YzCameraInfo camera, String type) throws Exception {
        // VISCA 光圈-：FF 01 02 00 00 00 03
        send(camera, buildViscaJson(type, replaceId("FF010200000003", type)), "镜头光圈-", type);
    }

    /** 停止所有动作 */
    @Override
    public void stop(YzCameraInfo camera, String type) throws Exception {
        // VISCA 停止命令：FF 01 00 00 00 00 01
        send(camera, buildViscaJson(type, replaceId("FF010060000061", type)), "停止所有动作", type);
    }

    /*===================== AI 自动跟踪控制 =====================*/

    /**
     * 开启 AI 自动跟踪
     * 对应 JSON:
     * {"ControlType":"SetWorkMode","SetWorkMode":2}
     */
    @Override
    public void startAiTrack(YzCameraInfo camera) throws Exception {
        JSONObject json = new JSONObject();
        json.put("ControlType", "SetWorkMode");
        json.put("SetWorkMode", 2);

        String jsonStr = json.toJSONString();
        byte[] frame = wrapFrame(jsonStr);

        log.info("发送命令 [AI自动跟踪-开启]: {}", jsonStr);
        log.info("封装帧 HEX [AI自动跟踪-开启]: {}", toHex(frame));
        camera.getGplAiCtrlTcpClient().sendInfo(frame);
    }

    /**
     * 关闭 AI 自动跟踪
     * 对应 JSON:
     * {"ControlType":"SetWorkMode","SetWorkMode":0}
     */
    @Override
    public void stopAiTrack(YzCameraInfo camera) throws Exception {
        JSONObject json = new JSONObject();
        json.put("ControlType", "SetWorkMode");
        json.put("SetWorkMode", 0);

        String jsonStr = json.toJSONString();
        byte[] frame = wrapFrame(jsonStr);

        log.info("发送命令 [AI自动跟踪-关闭]: {}", jsonStr);
        log.info("封装帧 HEX [AI自动跟踪-关闭]: {}", toHex(frame));
        camera.getGplAiCtrlTcpClient().sendInfo(frame);
    }

    /**
     * 云台方向控制
     * @param camera 相机信息
     * @param type   通道类型（1=可见光, 2=热像）
     * @param direction 方向 (UP,DOWN,LEFT,RIGHT,LEFT_UP,RIGHT_UP,LEFT_DOWN,RIGHT_DOWN)
     * @param speed 速度（1~100，自动映射至1~63）
     */
    @Override
    public void controlPTZ(YzCameraInfo camera, String type, String direction, int speed) throws Exception {
        int scaledSpeed = Math.min(63, Math.max(1, (int) (speed * 0.63)));

        byte addr = 0x01;
        byte cmd1 = 0x00;
        byte cmd2 = 0x00;
        byte panSpeed = 0x00;
        byte tiltSpeed = 0x00;

        switch (direction.toUpperCase()) {
            case "UP":        cmd2 = 0x08; tiltSpeed = (byte) scaledSpeed; break;
            case "DOWN":      cmd2 = 0x10; tiltSpeed = (byte) scaledSpeed; break;
            case "LEFT":      cmd2 = 0x04; panSpeed  = (byte) scaledSpeed; break;
            case "RIGHT":     cmd2 = 0x02; panSpeed  = (byte) scaledSpeed; break;
            case "LEFT_UP":   cmd2 = 0x0C; panSpeed  = (byte) scaledSpeed; tiltSpeed = (byte) scaledSpeed; break;
            case "RIGHT_UP":  cmd2 = 0x0A; panSpeed  = (byte) scaledSpeed; tiltSpeed = (byte) scaledSpeed; break;
            case "LEFT_DOWN": cmd2 = 0x14; panSpeed  = (byte) scaledSpeed; tiltSpeed = (byte) scaledSpeed; break;
            case "RIGHT_DOWN":cmd2 = 0x12; panSpeed  = (byte) scaledSpeed; tiltSpeed = (byte) scaledSpeed; break;
            default:
                log.warn("未知云台方向: {}", direction);
                return;
        }

        byte checksum = (byte) ((addr + cmd1 + cmd2 + panSpeed + tiltSpeed) & 0xFF);
        byte[] cmd = new byte[]{(byte) 0xFF, addr, cmd1, cmd2, panSpeed, tiltSpeed, checksum};

        String hexCmd = toHex(cmd).replace(" ", "");
        String jsonStr = buildViscaJson(type, replaceId(hexCmd, type));
        send(camera, jsonStr, "云台控制(" + direction + ", 速度=" + speed + ")", type);
    }

    /** 云台全停 */
    public void stopPTZ(YzCameraInfo camera, String type) throws Exception {
        String hexCmd = "FF010000000001";
        send(camera, buildViscaJson(type, replaceId(hexCmd, type)), "云台全停", type);
    }
}
