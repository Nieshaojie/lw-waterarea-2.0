package com.mskyeye.ws.platform;

import java.util.Collections;
import java.util.List;

/**
 * 机库及绑定无人机 VO（对应 get_dock_drone_sn 接口 data 元素）
 *
 * @author mskyeye
 */
public class DockDroneVO {

    /** 机库 ID */
    private Long dockId;

    /** 机库 SN */
    private String dockSn;

    /** 机库名称 */
    private String dockName;

    /** 该机库绑定的无人机列表 */
    private List<DroneVO> droneList = Collections.emptyList();

    public Long getDockId() {
        return dockId;
    }

    public void setDockId(Long dockId) {
        this.dockId = dockId;
    }

    public String getDockSn() {
        return dockSn;
    }

    public void setDockSn(String dockSn) {
        this.dockSn = dockSn;
    }

    public String getDockName() {
        return dockName;
    }

    public void setDockName(String dockName) {
        this.dockName = dockName;
    }

    public List<DroneVO> getDroneList() {
        return droneList;
    }

    public void setDroneList(List<DroneVO> droneList) {
        this.droneList = droneList;
    }
}