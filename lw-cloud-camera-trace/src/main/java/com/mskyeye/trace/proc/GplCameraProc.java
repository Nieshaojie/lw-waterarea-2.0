package com.mskyeye.trace.proc;

import com.mskyeye.trace.camera.utils.Utils;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.netty.control.GplCtrlTcpClientService;
import com.mskyeye.trace.utils.PanTiltCalculator;
import com.mskyeye.trace.utils.RadarGuideClientService;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.mskyeye.trace.common.GlResources.GL_CameraInfoMap;

/**
 * @ClassName:GplCameraProc
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/10 9:29
 * @Version:1.0
 **/
@Component
public class GplCameraProc {
    private static final Logger log = LoggerFactory.getLogger(GplCameraProc.class);
    @Autowired
    private RadarGuideClientService radarService;
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
    public boolean ptzControl(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal,Integer channelId) throws Exception {

//        boolean result = yzCameraInfo.getGplNetSDK().VSIF_VSPTZControlEx2(Long.valueOf(yzCameraInfo.getLoginInfo()),
//                0,0x43, (int) (pVal * 10),(int) (tVal * 10),(int) (zVal * 10),false,new Pointer(0));
//        if (!result)
//            System.out.println("错误码是:" + yzCameraInfo.getGplNetSDK().VSIF_GetLastError());
//        return result;
        byte addrCode = (channelId == 1) ? (byte) 0x01 : (byte) 0x02;
        System.out.println("p值："+pVal+"---t值："+tVal+"---z值："+zVal+"---通道号："+channelId);

        byte[] pValInfos = new byte[8];
        pValInfos[0] = (byte) 0xA7;
        pValInfos[1] = addrCode;
        pValInfos[2] = (byte) 0x02;
        pValInfos[3] = (byte) 0x02;
        Integer iPval = 0;
        //声光报警器的P值按照T值的计算方式来
//        if(yzCameraInfo.getIsAvAlarm() == 0){
//            iPval = (int) ((360.0 + yzCameraInfo.getAziMultiply() * yzCameraInfo.getAziZeroVal() - pVal) % 360.0 / yzCameraInfo.getAziMultiply());
//        }else if(yzCameraInfo.getIsAvAlarm() == 1){
//            iPval = (int) (yzCameraInfo.getAziZeroVal() - ((360.0 + pVal) % 360.0)/ yzCameraInfo.getAziMultiply());
//        }
//        iPval = (int) (yzCameraInfo.getAziZeroVal() - ((360.0 + pVal) % 360.0)/ yzCameraInfo.getAziMultiply());

//        if (yzCameraInfo.getIsAvAlarm() == 0) {
//            iPval = (int) (pVal/ yzCameraInfo.getAziMultiply() + yzCameraInfo.getAziZeroVal());
//            if(iPval > 65000){
//                iPval = iPval - 65000 + yzCameraInfo.getAziMinVal();
//            }
//        } else if (yzCameraInfo.getIsAvAlarm() == 1) {
//            iPval = (int) ((yzCameraInfo.getAziZeroVal() - pVal) / yzCameraInfo.getAziMultiply());
//        }

        iPval = (int) ((360 - pVal)/ yzCameraInfo.getAziMultiply() + yzCameraInfo.getAziZeroVal());
        if (iPval > 65000) {
            iPval = iPval - 65000 + yzCameraInfo.getAziMinVal();
        }

        byte[] pValArray = numConvert(iPval);
        pValInfos[4] = pValArray[0];
        pValInfos[5] = pValArray[1];
        pValInfos[6] = (byte) 0xC8;
        pValInfos[7] = checkBitFun(pValInfos);
//        System.out.println(byteArrayToHexString(pValInfos));
//        System.out.println("方位比值:" + yzCameraInfo.getAziMultiply()
//                + " 方位零值" + yzCameraInfo.getAziZeroVal() + " 方位最小值" + yzCameraInfo.getAziMinVal());
//        System.out.println("原P值:" + pVal + " 计算后的P值:" + iPval);
//        System.out.println("------------------------------------------");
        boolean b = yzCameraInfo.getGplCtrlTcpClient().sendInfo(pValInfos);
//        TimeUnit.MILLISECONDS.sleep(200);

        byte[] tValInfos = new byte[8];
        tValInfos[0] = (byte) 0xA7;
        tValInfos[1] = addrCode;
        tValInfos[2] = (byte) 0x02;
        tValInfos[3] = (byte) 0x03;
        Integer iTval = (int) (yzCameraInfo.getPitchZeroVal() - tVal / yzCameraInfo.getPitchMultiply());
        byte[] tValArray = numConvert(iTval);
        tValInfos[4] = tValArray[0];
        tValInfos[5] = tValArray[1];
        tValInfos[6] = (byte) 0xC8;
        tValInfos[7] = checkBitFun(tValInfos);
        boolean b1 = yzCameraInfo.getGplCtrlTcpClient().sendInfo(tValInfos);
//        TimeUnit.MILLISECONDS.sleep(200);

        byte[] zValInfos = new byte[8];
        zValInfos[0] = (byte) 0xA7;
        zValInfos[1] = addrCode;
        zValInfos[2] = (byte) 0x03;
        zValInfos[3] = (byte) 0x0C;
        Integer iZval = (int) (zVal * 16384 / 65);
        byte[] zValArray = numConvert(iZval);
        zValInfos[4] = zValArray[0];
        zValInfos[5] = zValArray[1];
        zValInfos[6] = (byte) 0x00;
        zValInfos[7] = checkBitFun(zValInfos);
        boolean b2 = yzCameraInfo.getGplCtrlTcpClient().sendInfo(zValInfos);
        System.out.println(b +"---"+ b1+"---" + b2);
//        TimeUnit.MILLISECONDS.sleep(200);
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

    public void aiTrackCtrl( TraceProInfo traceProInfo) throws Exception {
        log.info("当前目标信息：{}",traceProInfo.toString());
        YzCameraInfo yzCameraInfo = GL_CameraInfoMap.get(traceProInfo.getCameraId());
        if(traceProInfo.getTraceType() == 8) {
            //偏移校准值
            double pCorVal = yzCameraInfo.getAngle();
            double tCorVal = yzCameraInfo.gettVal();
            double height = yzCameraInfo.getHeight();
            //经纬高转换为方位值
            PanTiltCalculator.LatLonAlt eo = new PanTiltCalculator.LatLonAlt(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(), height);     // 光电位置
            PanTiltCalculator.LatLonAlt target = new PanTiltCalculator.LatLonAlt(traceProInfo.getTraceLat(), traceProInfo.getTraceLon(), 0);  // 目标位置
            PanTiltCalculator.PanTiltView ptv = PanTiltCalculator.calculate(eo, target,traceProInfo.getTargetWidth());
            //标记跟踪
            traceProInfo.setTraceType(8);
            try {
                // 开启ai跟踪
                Map<String, Object> startData = new HashMap<>();
                startData.put("msg", "guide_detect");
                startData.put("camid", traceProInfo.getChannelId());//通道号
                startData.put("start", 1);//1:开始联动   0 结束联动
                startData.put("alarmid", traceProInfo.getTargetId().toString());//报警ID
                startData.put("scenetype", 2);//AI检测场景 0 天空 1地面 2水面 3不检测
                startData.put("presetid", -1);//默认字段，填-1
                startData.put("pan", (ptv.pan - pCorVal + 360) % 360);//雷达引导需跳转方位值(添加校准)
                startData.put("tilt", ptv.tilt - tCorVal);//雷达引导需跳转俯仰值
                startData.put("view", ptv.view);//雷达引导需跳转的视场值
                startData.put("detecttimeout", 30);//AI检测超时时间
                startData.put("tracktimeout", 300);//跟踪超时时间
                radarService.sendStartGuide(startData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(traceProInfo.getTraceType() == 9) {
            try {
                // 取消ai跟踪
                Map<String, Object> startData = new HashMap<>();
                startData.put("msg", "guide_detect");
                startData.put("camid", traceProInfo.getChannelId());//通道号
                startData.put("start", 0);//1:开始联动   0 结束联动
                startData.put("alarmid", traceProInfo.getTargetId());//报警ID
                startData.put("scenetype", 2);//AI检测场景 0 天空 1地面 2水面 3不检测
                startData.put("presetid", 0);//默认字段，填-1
                startData.put("pan", 0.0);//雷达引导需跳转方位值
                startData.put("tilt", 0.0);//雷达引导需跳转俯仰值
                startData.put("view", 0.0);//雷达引导需跳转的视场值
                startData.put("detecttimeout", 0);//AI检测超时时间
                startData.put("tracktimeout", 0);//跟踪超时时间
                radarService.sendStartGuide(startData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
