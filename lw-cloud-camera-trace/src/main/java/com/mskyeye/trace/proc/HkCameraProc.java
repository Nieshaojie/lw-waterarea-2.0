package com.mskyeye.trace.proc;

import com.mskyeye.trace.camera.hkws.callback.AlarmCallback;
import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.mskyeye.trace.camera.hkws.struct.*;
import com.mskyeye.trace.model.TraceProInfo;
import com.mskyeye.trace.model.YzCameraInfo;
import com.mskyeye.trace.utils.DisAndAngleUtils;
import com.mskyeye.trace.utils.HkNetSDKLoader;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mskyeye.trace.camera.hkws.sdk.HkNetSDK.NET_DVR_SET_PTZPOS;

/**
 * @ClassName:HikCameraProc
 * @Description:海康相机功能类
 * @Author:R.Gong
 * @Date:2023/9/5 15:43
 * @Version:1.0
 **/
@Component
public class HkCameraProc {

    private static final Logger log = LoggerFactory.getLogger(HkCameraProc.class);
    @Autowired
    public AlarmCallback callback;

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
    public boolean ptzControl(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {
        NET_DVR_PTZPOS lpPtzPos = new NET_DVR_PTZPOS();
        Integer iTVal = (int) (tVal * 10);
        if (iTVal < 0) {
            iTVal = 3600 + iTVal;
        }
        lpPtzPos.wAction = 1;
        lpPtzPos.wPanPos = (short) cvt2Hex((int) (pVal * 10));
        lpPtzPos.wTiltPos = (short) cvt2Hex(iTVal);
        lpPtzPos.wZoomPos = (short) cvt2Hex((int) (zVal * 10));
        lpPtzPos.write();
        boolean result = yzCameraInfo.getHkNetSDK().NET_DVR_SetDVRConfig(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())),
                NET_DVR_SET_PTZPOS, new NativeLong(1), lpPtzPos.getPointer(), lpPtzPos.size());
        if (!result) {
            System.out.println("错误码是:" + yzCameraInfo.getHkNetSDK().NET_DVR_GetLastError());
            System.out.println("错误信息打印:" + lpPtzPos);
//            //先登出
//            yzCameraInfo.getHkNetSDK().NET_DVR_Logout(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())));
//            //海康相机重新登录并更改信息
//            GL_CameraInfoMap.put(yzCameraInfo.getId(),hikLogin(yzCameraInfo));
        }
        return result;
    }


    /**
     * 框选跟踪
     *
     * @param yzCameraInfo
     * @param positionX
     * @param positionY
     * @param positionX2
     * @param positionY2
     * @return
     * @throws Exception
     */
    public boolean boxTrackCtrl(YzCameraInfo yzCameraInfo, Integer channelid,
                                Integer positionX, Integer positionY,
                                Integer positionX2, Integer positionY2) throws Exception {
        NET_DVR_XML_CONFIG_INPUT struInput = new NET_DVR_XML_CONFIG_INPUT();
        String requetUrl = "PUT /ISAPI/PTZCtrl/channels/" + channelid + "/ManualTrace\r\n";

        struInput.dwSize = struInput.size();
        struInput.lpRequestUrl = new Memory(requetUrl.length() + 1);
        struInput.lpRequestUrl.setString(0, requetUrl);
        struInput.dwRequestUrlLen = requetUrl.length();
        String requestBody = String.format("<ManualTrace><positionX>%d</positionX><positionY>%d</positionY><positionX2>%d</positionX2><positionY2>%d</positionY2></ManualTrace>",
                positionX, positionY, positionX2, positionY2);
        struInput.lpInBuffer = new Memory(requestBody.length() + 1);
        struInput.lpInBuffer.setString(0, requestBody);
        struInput.dwInBufferSize = requestBody.length();
        struInput.dwRecvTimeOut = 5000;
        struInput.byForceEncrpt = 0;
        struInput.byNumOfMultiPart = 0;


        NET_DVR_XML_CONFIG_OUTPUT struOutput = new NET_DVR_XML_CONFIG_OUTPUT();
        struOutput.dwSize = struOutput.size();
        struOutput.lpOutBuffer = new Memory(10 * 1024);
        struOutput.dwOutBufferSize = 10 * 1024;
        struOutput.dwReturnedXMLSize = 0;
        struOutput.lpStatusBuffer = new Memory(1024);
        struOutput.dwStatusSize = 1024;

        if (!yzCameraInfo.getHkNetSDK().NET_DVR_STDXMLConfig(new NativeLong(Long.valueOf(yzCameraInfo.getLoginInfo())),
                struInput, struOutput)) {
            System.out.println("错误码是:" + yzCameraInfo.getHkNetSDK().NET_DVR_GetLastError());
            return false;
        } else {
            return true;
        }
    }

    /**
     * 开启海康图像跟踪
     *
     * @param traceProInfo
     * @param yzCameraInfo
     * @return
     */
    public boolean photoTraceStart(TraceProInfo traceProInfo, YzCameraInfo yzCameraInfo) {

        Double f_Ele = yzCameraInfo.getCurTVal();
        Double f_Azi = (yzCameraInfo.getCurPVal() - yzCameraInfo.getAngle() + 360) % 360;
        Double f_Dis = DisAndAngleUtils.get_distance(yzCameraInfo.getLat().doubleValue(), yzCameraInfo.getLon().doubleValue(),
                traceProInfo.getTraceLat(), traceProInfo.getTraceLon());
        String szUrl = "PUT /ISAPI/PTZCtrl/channels/1/absoluteEx\r\n";
        String pBuf = String.format("<PTZAbsoluteEx version=\"2.0\" " +
                "xmlns=\"http://www.hikvision.com/ver20/XMLSchema\">" +
                "<elevation>%f</elevation>" +
                "<azimuth>%f</azimuth>" +
                "<horizontalSpeed>1.00</horizontalSpeed>" +
                "<verticalSpeed>1.00</verticalSpeed>" +
                "<objectDistance>%f</objectDistance>" +
                "<isContinuousTrackingEnabled>false</isContinuousTrackingEnabled>" +
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
     * 十进制转十六进制
     *
     * @param iValue
     * @return
     */
    private int cvt2Hex(int iValue) {
//        return (iValue / 1000) * 4096 + ((iValue % 1000) / 100) * 256 + ((iValue % 100) / 10) * 16 + iValue % 10;
        // 将输入限制在有效的角度范围内（假设范围是0~3600）
        if (iValue < 0) {
            iValue = 3600 + iValue;  // 确保负值转换为有效范围
        }
        iValue = iValue % 3600;  // 保证不超过3600
        return (iValue / 1000) * 4096 + ((iValue % 1000) / 100) * 256 + ((iValue % 100) / 10) * 16 + iValue % 10;
    }


    /**
     * 海康相机登录
     *
     * @param yzCameraInfo
     * @return
     */
    public YzCameraInfo hikLogin(YzCameraInfo yzCameraInfo) {
        //TODO windows
//        String url = "win32-x86-64/hk_lib/win/HCNetSDK.dll";
        //TODO Linux
//        String url = "/home/hk_lib/linux/libhcnetsdk.so";
        //初始化dll、连接设备
        //TODO
//        HkNetSDK hkNetSDK = Native.load(url, HkNetSDK.class);//调试时
        HkNetSDK hkNetSDK = HkNetSDKLoader.loadSDK();
        NET_DVR_DEVICEINFO_V30 netDvrDeviceinfoV30 = new NET_DVR_DEVICEINFO_V30();
        hkNetSDK.NET_DVR_Init();//初始化
        //相机登录
        NET_DVR_USER_LOGIN_INFO struLoginInfo = new NET_DVR_USER_LOGIN_INFO();
        NET_DVR_DEVICEINFO_V40 struDeviceInfo = new NET_DVR_DEVICEINFO_V40();
        Pointer PointerstruDeviceInfoV40 = struDeviceInfo.getPointer();
        Pointer PointerstruLoginInfo = struLoginInfo.getPointer();

        for (int i = 0; i < yzCameraInfo.getIp().length(); i++) {
            struLoginInfo.sDeviceAddress[i] = (byte) yzCameraInfo.getIp().charAt(i);
        }
        for (int i = 0; i < yzCameraInfo.getPassWord().length(); i++) {
            struLoginInfo.sPassword[i] = (byte) yzCameraInfo.getPassWord().charAt(i);
        }
        for (int i = 0; i < yzCameraInfo.getUserName().length(); i++) {
            struLoginInfo.sUserName[i] = (byte) yzCameraInfo.getUserName().charAt(i);
        }
        struLoginInfo.wPort = Short.valueOf(yzCameraInfo.getManPort().toString());
        struLoginInfo.write();
        NativeLong loginId = hkNetSDK.NET_DVR_Login_V40(PointerstruLoginInfo, PointerstruDeviceInfoV40);//登录
        if(loginId.intValue()>=0){
            System.out.println("当前登录相机信息："+yzCameraInfo);
        }else {
//            System.out.println("登录失败相机信息："+yzCameraInfo);
        }
        Integer error = hkNetSDK.NET_DVR_GetLastError();

        //存入登录ID
        yzCameraInfo.setLoginInfo(String.valueOf(loginId));
        yzCameraInfo.setHkNetSDK(hkNetSDK);
//        TODO 修改回调函数方法参数
        try {
            if (yzCameraInfo.getName().equals("大英界") || yzCameraInfo.getName().equals("叶郢渡口")) {
                //注册回调函数
                hkNetSDK.NET_DVR_SetDVRMessageCallBack_V50(0, callback, null);
                //启用布防上传通道
                NET_DVR_SETUPALARM_PARAM struAlarmParam = new NET_DVR_SETUPALARM_PARAM();
                Integer lHandle = hkNetSDK.NET_DVR_SetupAlarmChan_V41(loginId, struAlarmParam);
                if (lHandle >= 0) {
                    yzCameraInfo.setlHandle(lHandle);
                    log.info("布防通道启用成功，lHandle = {}", lHandle);
                }else {
                    int errorCode = hkNetSDK.NET_DVR_GetLastError();
                    log.error("布防通道启用失败，错误码: {}", errorCode);
                }
            }
        }catch (Exception e){
            log.error("启用布防上传通道时发生异常", e);
        }
//        System.out.println("hikLogin 相机登录信息："+yzCameraInfo);
        return yzCameraInfo;
    }
}
