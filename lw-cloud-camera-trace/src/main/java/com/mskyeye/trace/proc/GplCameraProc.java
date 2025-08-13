package com.mskyeye.trace.proc;

import com.mskyeye.trace.camera.utils.Utils;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.GplCtrlTcpClientService;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
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
        TimeUnit.MILLISECONDS.sleep(30);
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
        TimeUnit.MILLISECONDS.sleep(30);
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
        TimeUnit.MILLISECONDS.sleep(30);
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
}
