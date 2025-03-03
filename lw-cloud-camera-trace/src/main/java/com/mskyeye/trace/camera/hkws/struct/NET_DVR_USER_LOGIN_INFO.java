package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.hkws.callback.fLoginResultCallBack;
import com.mskyeye.trace.camera.utils.SdkStructure;
import com.sun.jna.Pointer;

/**
 * @ClassName:NET_DVR_USER_LOGIN_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 16:18
 * @Version:1.0
 **/
public class NET_DVR_USER_LOGIN_INFO extends SdkStructure {

    public byte[] sDeviceAddress = new byte[129];
    public byte byUseTransport;
    public short wPort;
    public byte[] sUserName = new byte[64];
    public byte[] sPassword = new byte[64];
    public fLoginResultCallBack cbLoginResult;
    public Pointer pUser;
    public int bUseAsynLogin; // BOOL is represented as an int in JNA
    public byte byProxyType;
    public byte byUseUTCTime;
    public byte byLoginMode;
    public byte byHttps;
    public int iProxyID; // LONG is represented as an int in JNA
    public byte byVerifyMode;
    public byte[] byRes3 = new byte[119];

}
