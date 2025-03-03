package com.mskyeye.trace.model;

/**
 * @ClassName:DeviceMonitorInfo
 * @Description:设备监控信息(水天科技)
 * @Author:R.Gong
 * @Date:2023/8/29 11:49
 * @Version:1.0
 **/
public class DeviceMonitorInfo {

    private String msgType;

    private DeviceMonitorInfoBody object;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public DeviceMonitorInfoBody getObject() {
        return object;
    }

    public void setObject(DeviceMonitorInfoBody object) {
        this.object = object;
    }
}
