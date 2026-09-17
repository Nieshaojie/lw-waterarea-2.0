package com.mskyeye.ws.platform;

/**
 * 无人机 VO（对应 get_dock_drone_sn 接口 droneList 元素）
 *
 * @author mskyeye
 */
public class DroneVO {

    /** 无人机 ID */
    private Long droneId;

    /** 无人机 SN */
    private String droneSn;

    /** 无人机名称 */
    private String droneName;

    public Long getDroneId() {
        return droneId;
    }

    public void setDroneId(Long droneId) {
        this.droneId = droneId;
    }

    public String getDroneSn() {
        return droneSn;
    }

    public void setDroneSn(String droneSn) {
        this.droneSn = droneSn;
    }

    public String getDroneName() {
        return droneName;
    }

    public void setDroneName(String droneName) {
        this.droneName = droneName;
    }
}