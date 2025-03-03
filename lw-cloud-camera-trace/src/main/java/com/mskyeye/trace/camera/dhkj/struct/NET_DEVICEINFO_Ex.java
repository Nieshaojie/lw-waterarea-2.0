package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DEVICEINFO_Ex
 * @Description:设备信息扩展
 * @Author:R.Gong
 * @Date:2023/9/6 11:18
 * @Version:1.0
 **/
public class NET_DEVICEINFO_Ex extends SdkStructure {
    public byte[]     sSerialNumber = new byte[48];    // 序列号
    public int        byAlarmInPortNum;                              // DVR报警输入个数
    public int        byAlarmOutPortNum;                             // DVR报警输出个数
    public int        byDiskNum;                                     // DVR硬盘个数
    public int        byDVRType;                                     // DVR类型,见枚举NET_DEVICE_TYPE
    public int        byChanNum;                                     // DVR通道个数
    public byte       byLimitLoginTime;                              // 在线超时时间,为0表示不限制登陆,非0表示限制的分钟数
    public byte       byLeftLogTimes;                                // 当登陆失败原因为密码错误时,通过此参数通知用户,剩余登陆次数,为0时表示此参数无效
    public byte[]     bReserved = new byte[2];                       // 保留字节,字节对齐
    public int        byLockLeftTime;                                // 当登陆失败,用户解锁剩余时间（秒数）, -1表示设备未设置该参数
    public byte[]     Reserved = new byte[24];                       // 保留


}
