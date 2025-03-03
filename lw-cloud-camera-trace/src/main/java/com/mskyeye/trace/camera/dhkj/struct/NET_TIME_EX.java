package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_TIME_EX
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 11:34
 * @Version:1.0
 **/
public class NET_TIME_EX extends SdkStructure {
    public int dwYear;
    public int dwMonth;
    public int dwDay;
    public int dwHour;
    public int dwMinute;
    public int dwSecond;
    public int dwMillisecond;
    public int dwUTC;
    public int[] dwReserved = new int[1];
}
