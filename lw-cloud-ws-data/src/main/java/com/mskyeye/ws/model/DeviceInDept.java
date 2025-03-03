package com.mskyeye.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @ClassName:CameraAndRadarInDept
 * @Description:机构内的相机和雷达信息
 * @Author:R.Gong
 * @Date:2023/8/23 16:08
 * @Version:1.0
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInDept {

    private Long deptId;

    private List<Long> cameraIdList;

    private List<Long> radarIdList;

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public List<Long> getCameraIdList() {
        return cameraIdList;
    }

    public void setCameraIdList(List<Long> cameraIdList) {
        this.cameraIdList = cameraIdList;
    }

    public List<Long> getRadarIdList() {
        return radarIdList;
    }

    public void setRadarIdList(List<Long> radarIdList) {
        this.radarIdList = radarIdList;
    }
}
