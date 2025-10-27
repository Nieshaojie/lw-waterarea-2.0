package com.mskyeye.trace.proc;

import com.mskyeye.trace.camera.utils.Utils;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.GplCtrlTcpClientService;
import com.mskyeye.trace.netty.control.service.CameraLensControl;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName:GplCameraProc
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/10 9:29
 * @Version:1.0
 **/
@Component
@Slf4j
public class GplCameraProc {

    /**
     * 通过PTZ直接引导相机
     *
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @param zVal
     * @return
     * @throws Exception
     */
    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public boolean ptzControl(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {

        byte[] pValInfos = new byte[8];
        pValInfos[0] = (byte) 0xA7;
        pValInfos[1] = (byte) 0x01;
        pValInfos[2] = (byte) 0x02;
        pValInfos[3] = (byte) 0x02;

        Integer iPval = (int) ((360 - pVal)/ yzCameraInfo.getAziMultiply() + yzCameraInfo.getAziZeroVal());
        if (iPval > 65000) {
            iPval = iPval - 65000 + yzCameraInfo.getAziMinVal();
        }
        byte[] pValArray = numConvert(iPval);
        pValInfos[4] = pValArray[0];
        pValInfos[5] = pValArray[1];
        pValInfos[6] = (byte) 0xC8;
        pValInfos[7] = checkBitFun(pValInfos);

        // 打印日志
        System.out.println("发送 P 指令:"+ toHexString(pValInfos));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(pValInfos);

        byte[] tValInfos = new byte[8];
        tValInfos[0] = (byte) 0xA7;
        tValInfos[1] = (byte) 0x01;
        tValInfos[2] = (byte) 0x02;
        tValInfos[3] = (byte) 0x03;
        Integer iTval = (int) (yzCameraInfo.getPitchZeroVal() - tVal / yzCameraInfo.getPitchMultiply());
        byte[] tValArray = numConvert(iTval);
        tValInfos[4] = tValArray[0];
        tValInfos[5] = tValArray[1];
        tValInfos[6] = (byte) 0xC8;
        tValInfos[7] = checkBitFun(tValInfos);

        System.out.println("发送 T 指令:"+ toHexString(tValInfos));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(tValInfos);

        byte[] zValInfos = new byte[8];
        zValInfos[0] = (byte) 0xA7;
        zValInfos[1] = (byte) 0x01;
        zValInfos[2] = (byte) 0x03;
        zValInfos[3] = (byte) 0x0C;
        Integer iZval = (int) (zVal * 16384 / 65);
        byte[] zValArray = numConvert(iZval);
        zValInfos[4] = zValArray[0];
        zValInfos[5] = zValArray[1];
        zValInfos[6] = (byte) 0x00;
        zValInfos[7] = checkBitFun(zValInfos);
        System.out.println("发送 Z 指令:" + toHexString(zValInfos));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(zValInfos);
        return true;
    }

    private String byteArrayToHexString(byte[] pValInfos) {
        StringBuilder sb = new StringBuilder();
        for (byte b : pValInfos) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    private static final byte[] openLightOrder = {(byte) 0xA7,(byte) 0x01,(byte) 0x01,(byte) 0x01,
            (byte) 0x05,(byte) 0x02,(byte) 0x00,(byte) 0x0A};

    private static final byte[] closeLightOrder = {(byte) 0xA7,(byte) 0x01,(byte) 0x01,(byte) 0x01,
            (byte) 0x06,(byte) 0x02,(byte) 0x00,(byte) 0x0B};

    public boolean lightControl(YzCameraInfo yzCameraInfo, Integer isOpenLight) throws Exception{
        //关闭强光
        if(isOpenLight == 0){
            yzCameraInfo.getGplCtrlTcpClient().sendInfo(closeLightOrder);
        }else if(isOpenLight == 1){
            yzCameraInfo.getGplCtrlTcpClient().sendInfo(openLightOrder);
        }
        return true;
    }
    private void printByteInfo(byte[] bytes) {
        // 将 byte 数组转换为十六进制字符串（大写形式）
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
            hexString.append(" ");
        }

        String hexStringResult = hexString.toString();

        System.out.println("Uppercase Hexadecimal String: " + hexStringResult);
    }

    /**
     * 整型转换成字节数组
     * @param num
     * @return
     * @throws Exception
     */
    private byte[] numConvert(Integer num) throws Exception {
        // 将整数转换为十六进制字符串
        String hexString = Integer.toHexString(num);

        // 在前面补0补足4位
        hexString = String.format("%4s", hexString).replace(' ', '0');

        // 拆分成前2位和后2位
        String firstPart = hexString.substring(0, 2); // 前2位
        String secondPart = hexString.substring(2); // 后2位

        // 将十六进制字符串转换为 byte
        byte byte1 = (byte) Short.parseShort(firstPart, 16);
        byte byte2 = (byte) Short.parseShort(secondPart, 16);
        byte[] result = {byte1,byte2};
        return result;
    }

    /**
     * 获取校验位
     * @param numBytes
     * @return
     * @throws Exception
     */
    private byte checkBitFun(byte[] numBytes) throws Exception{
        int sum = 0;
        // 从第二个字节开始遍历，累加每个字节的十六进制值
        for (int i = 1; i < numBytes.length - 1; i++) {
            sum += numBytes[i] & 0xFF; // 将 byte 转换为无符号整数
        }

        // 取累加值的后两位数
        int lastTwoDigits = sum % 0x100;
        // 将后两位数转换为byte类型
        return (byte) lastTwoDigits;
    }

    /**
     * 相机联动跟踪方位控制(SDK开发使用)
     * 转动方向rotaDir：0 左 1 右
     *
     * @param yzCameraInfo
     * @param rotaDir
     * @return
     * @throws Exception
     */
    public boolean aziControl(YzCameraInfo yzCameraInfo, Integer rotaDir, boolean bStop) {
        try {
            int dwPTZCommand = rotaDir == 0 ? 2 : 3;
            boolean result = yzCameraInfo.getGplNetSDK().VSIF_VSPTZControlEx2(Long.valueOf(yzCameraInfo.getLoginInfo()),
                    0, dwPTZCommand, 8, 0, 0, bStop, new Pointer(0));
            TimeUnit.MILLISECONDS.sleep(200);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 高普乐雷球联动跟踪(串口方式)
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @param zVal
     * @return
     */
    public boolean gplLinkTrace(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        GplCtrlTcpClientService gplCtrlTcpClient = yzCameraInfo.getGplCtrlTcpClient();

        byte[] pValInfos = new byte[8];
        pValInfos[0] = (byte) 0xA7;
        pValInfos[1] = (byte) 0x01;
        pValInfos[2] = (byte) 0x04;
        pValInfos[3] = (byte) 0x0D;
        Integer iPval = (int) ((360 - pVal)/ yzCameraInfo.getAziMultiply() + yzCameraInfo.getAziZeroVal());
        if (iPval > 65000) {
            iPval = iPval - 65000 + yzCameraInfo.getAziMinVal();
        }
        byte[] pValArray = numConvert(iPval);
        pValInfos[4] = pValArray[0];
        pValInfos[5] = pValArray[1];
        pValInfos[6] = (byte) 0x00;
        pValInfos[7] = checkBitFun(pValInfos);
        gplCtrlTcpClient.sendInfo(pValInfos);

        byte[] tValInfos = new byte[8];
        tValInfos[0] = (byte) 0xA7;
        tValInfos[1] = (byte) 0x01;
        tValInfos[2] = (byte) 0x04;
        tValInfos[3] = (byte) 0x0E;
        Integer iTval = (int) (yzCameraInfo.getPitchZeroVal() - tVal / yzCameraInfo.getPitchMultiply());
        byte[] tValArray = numConvert(iTval);
        tValInfos[4] = tValArray[0];
        tValInfos[5] = tValArray[1];
        tValInfos[6] = (byte) 0x00;
        tValInfos[7] = checkBitFun(tValInfos);
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(tValInfos);

        System.out.println("发送给光电【"+yzCameraInfo.getName()+"】PT值【"+iPval
                +","+ iTval
                + "】 当前时间:" + Utils.getDate());

        return true;
    }


    // 命令字定义（根据 PELCO-DV1.7）
    private static final byte CMD_PAN_TO  = 0x4B; // 指定水平至设定位置
    private static final byte CMD_TILT_TO = 0x4D; // 指定俯仰至设定位置
    private static final byte CMD_ZOOM_TO = 0x4F; // 指定变倍至设定位置

    private static final byte DEFAULT_ADDR = 0x01;
    private static final byte DEFAULT_SPEED = 0x3F;

    /**
     * 根据 PELCO-DV1.7 协议，控制云台水平、俯仰、变倍至指定位置。
     * @param yzCameraInfo 摄像机信息（用于发送通道）
     * @param pVal 水平角（度，0~360）
     * @param tVal 俯仰角（度，0~360，0°向下角度值增大）
     * @param zVal 变倍值（0~0x4000）
     */
    public boolean ptzControlPD(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        // ---- 水平控制 ----
        Integer iPval = (int) ((360 - pVal)/ yzCameraInfo.getAziMultiply() + yzCameraInfo.getAziZeroVal());
        byte[] panCmd = buildPelcoDFrame(DEFAULT_ADDR, CMD_PAN_TO, encodeAngle(pVal));
        log.info("发送水平控制: {}", toHex(panCmd));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(panCmd);

        Thread.sleep(25);

        // ---- 俯仰控制 ----
        //Integer iTval = (int) (yzCameraInfo.getPitchZeroVal() + tVal / yzCameraInfo.getPitchMultiply());
        byte[] tiltCmd = buildPelcoDFrame(DEFAULT_ADDR, CMD_TILT_TO, encodeTilt(tVal));
        log.info("发送俯仰控制: {}", toHex(tiltCmd));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(tiltCmd);

        Thread.sleep(25);

        // ---- 变倍控制 ----
//        Integer iZval = (int) (zVal * 16384 / 65);
        byte[] zoomCmd = buildPelcoDFrameZ(DEFAULT_ADDR, CMD_ZOOM_TO, encodeZoom( zVal));
        System.out.println(toHex(zoomCmd));
        yzCameraInfo.getGplCtrlTcpClient().sendInfo(zoomCmd);
        return true;
    }

    /**
     * 构造标准帧：FF XX 00 CMD D1 D2 SUM
     */
    private byte[] buildPelcoDFrame(int addr, int cmd, int[] dataBytes) {
        byte[] frame = new byte[7];
        frame[0] = (byte) 0xFF;
        frame[1] = (byte) addr;
        frame[2] = DEFAULT_SPEED; // 速度位默认0
        frame[3] = (byte) cmd;
        frame[4] = (byte) dataBytes[0];
        frame[5] = (byte) dataBytes[1];
        frame[6] = calcChecksum(frame);
        return frame;
    }

    private byte[] buildPelcoDFrameZ(int addr, int cmd, int[] dataBytes) {
        byte[] frame = new byte[7];
        frame[0] = (byte) 0xFF;
        frame[1] = (byte) addr;
        frame[2] = 0x00; // 速度位默认0
        frame[3] = (byte) cmd;
        frame[4] = (byte) dataBytes[0];
        frame[5] = (byte) dataBytes[1];
        frame[6] = calcChecksum(frame);
        return frame;
    }

    /**
     * 校验和：从第2字节到第6字节累加取低8位
     */
    private byte calcChecksum(byte[] frame) {
        int sum = 0;
        for (int i = 1; i <= 5; i++) {
            sum += (frame[i] & 0xFF);
        }
        return (byte) (sum & 0xFF);
    }

    /**
     * 水平角编码：角度×100
     * 例：45° -> 4500 -> 0x11 0x94
     */
    private int[] encodeAngle(double degrees) {
        degrees = normalize360(degrees);
        int val = (int) Math.round(degrees * 100);
        return new int[]{(val >> 8) & 0xFF, val & 0xFF};
    }

    /**
     * 俯仰角编码：0°向下角度值增大，0°向上为360°递减
     * 如果你输入的 tVal 是“物理意义上的角度（向上为正）”，
     * 则需转换为协议角度。
     */
    private int[] encodeTilt(double tVal) {
        // 归一化物理角度到0~360
        tVal = ((tVal % 360) + 360) % 360;

        // 协议角度映射：0°向下值增大，向上递减
        double protoAngle = 360 - tVal; // 协议角度
        protoAngle = (protoAngle + 360) % 360; // 保证0~360

        // 转换为协议值，单位100
        int val = (int) Math.round(protoAngle * 100);
        // 高低位拆分
        int high = (val >> 8) & 0xFF;
        int low = val & 0xFF;

        return new int[]{high, low};
    }


    /**
     * 变倍编码：0x0000 - 0x4000
     */
    private int[] encodeZoom(double zoomVal) {
        int val = (int) Math.round(Math.max(0, Math.min(zoomVal, 0x4000)));
        return new int[]{(val >> 8) & 0xFF, val & 0xFF};
    }

    private double normalize360(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private String toHex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }
}
