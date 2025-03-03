package com.mskyeye.trace.camera.hkws.sdk;

import com.mskyeye.trace.camera.hkws.callback.MSGCallBack;
import com.mskyeye.trace.camera.hkws.struct.*;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * @ClassName:HCNetSDK
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/5 15:14
 * @Version:1.0
 **/
public interface HkNetSDK extends Library {

    /**常量 */
    public static final int SERIALNO_LEN = 48;   //序列号长度
    public static final int NET_DVR_SET_PTZPOS = 292;		//云台设置PTZ位置
    public static final int NET_DVR_GET_PTZPOS = 293;		//云台获取PTZ位置

    /**海康接口方法 */
    boolean  NET_DVR_Init();
    int  NET_DVR_GetLastError();
//    NativeLong NET_DVR_Login_V30(String sDVRIP, short wDVRPort, String sUserName, String sPassword, NET_DVR_DEVICEINFO_V30 lpDeviceInfo);
//    boolean  NET_DVR_Logout_V30(NativeLong lUserID);
    NativeLong NET_DVR_Login_V40(Pointer pLoginInfo,Pointer lpDeviceInfo);
    boolean  NET_DVR_Logout(NativeLong lUserID);
    boolean  NET_DVR_Cleanup();
    boolean  NET_DVR_GetDVRConfig(NativeLong lUserID, int dwCommand, NativeLong lChannel, Pointer lpOutBuffer, int dwOutBufferSize, IntByReference lpBytesReturned);
    boolean  NET_DVR_PTZControlWithSpeed_Other(NativeLong lUserID, NativeLong lChannel, int dwPTZCommand, int dwStop, int dwSpeed);
    boolean  NET_DVR_SetDVRConfig(NativeLong lUserID, int dwCommand, NativeLong lChannel, Pointer lpInBuffer, int dwInBufferSize);
    boolean NET_DVR_STDXMLConfig(NativeLong lUserID, NET_DVR_XML_CONFIG_INPUT lpInputParam, NET_DVR_XML_CONFIG_OUTPUT lpOutputParam);

    //来安船只告警

    int NET_DVR_SetupAlarmChan_V41(NativeLong lUserID, NET_DVR_SETUPALARM_PARAM lpSetupParam);//启用布防上传通道
    int NET_DVR_CloseAlarmChan_V30(int lAlarmHandle);//撤销布防上传通道

    boolean NET_DVR_SetDVRMessageCallBack_V50(int iIndex, MSGCallBack fMessageCallBack, Pointer pUser);



    public static interface FMSGCallBack extends StdCallLibrary.StdCallCallback {
        public void invoke(int lCommand, NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser);
    }
}
