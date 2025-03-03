package com.mskyeye.trace.camera.gpl.sdk;

import com.mskyeye.trace.camera.gpl.callback.fDisConnect;
import com.mskyeye.trace.camera.gpl.callback.fHaveReConnect;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface GplNetSDK extends Library {

    /**常量 */
    public static final int GPL_DEVSTATE_PTZ_LOCATION   = 0x0036;  // 查询云台状态信息

    /**接口方法 */
    //  JNA直接调用方法定义，cbDisConnect实际情况并不回调Java代码，仅为定义可以使用如下方式进行定义。
    boolean VSIF_Init(fDisConnect cbDisConnect, NativeLong dwUser);

    //  JNA直接调用方法定义，SDK退出清理
    void VSIF_Cleanup();

    //  JNA直接调用方法定义，向设备注销
    boolean VSIF_Logout(Long lLoginID);

    //  JNA直接调用方法定义，设置断线重连成功回调函数，设置后SDK内部断线自动重连
    void VSIF_SetAutoReconnect(fHaveReConnect cbAutoConnect, NativeLong dwUser);

    // 返回函数执行失败代码
    int VSIF_GetLastError();

    // 设置连接设备超时时间和尝试次数
    void VSIF_SetConnectTime(int nWaitTime, int nTryTimes);

    //  JNA直接调用方法定义，登陆扩展接口///////////////////////////////////////////////////
    //  nSpecCap 对应  EM_LOGIN_SPAC_CAP_TYPE 登陆类型
    Long VSIF_LoginEx2(String pchDVRIP, short wDVRPort, String pchUserName, String pchPassword, int nSpecCap, Pointer pCapParam, int lpDeviceInfo, int[] error/*= 0*/);

    // 云台控制扩展接口,支持三维快速定位,鱼眼
    // dwStop类型为BOOL, 取值0或者1
    // dwPTZCommand取值为NET_EXTPTZ_ControlType中的值或者是NET_PTZ_ControlType中的值
    boolean VSIF_VSPTZControlEx2(Long lLoginID, int nChannelID, int dwPTZCommand, long lParam1, long lParam2, long lParam3, boolean dwStop, Pointer param4);

    boolean VSIF_QueryDevState(Long lLoginID, int nType, Pointer pBuf, int nBufLen, IntByReference pRetLen, int waittime);
}
