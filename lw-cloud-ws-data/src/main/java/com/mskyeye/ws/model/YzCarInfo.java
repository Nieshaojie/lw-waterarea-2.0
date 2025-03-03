package com.mskyeye.ws.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 执法车辆信息对象 yz_car_info
 * 
 * @author ruoyi
 * @date 2023-10-22
 */
public class YzCarInfo
{
    private static final long serialVersionUID = 1L;

    /** 执法车辆id */
    private Long id;

    /** 执法车辆编号 */
    private String carCode;

    /** 执法车辆型号 */
    private String carModel;

    /** 执法车辆车牌号 */
    private String carPlateNumber;

    /** 执法车辆描述 */
    private String carDescribe;

    /** 部门id */
    private Long deptId;

    /** 执法车辆照片 */
    private String addrUrl;

    /** 执法车辆gps */
    private String carGps;

    /** 执法车辆经度 */
    private Double lon;

    /** 执法车辆纬度 */
    private Double lat;

    /** 执法车辆状态 */
    private Integer carStatus = 3;//1:行驶中 2:停车 3:驻车
    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCarCode(String carCode) 
    {
        this.carCode = carCode;
    }

    public String getCarCode() 
    {
        return carCode;
    }
    public void setCarModel(String carModel) 
    {
        this.carModel = carModel;
    }

    public String getCarModel() 
    {
        return carModel;
    }
    public void setCarPlateNumber(String carPlateNumber) 
    {
        this.carPlateNumber = carPlateNumber;
    }

    public String getCarPlateNumber() 
    {
        return carPlateNumber;
    }
    public void setCarDescribe(String carDescribe) 
    {
        this.carDescribe = carDescribe;
    }

    public String getCarDescribe() 
    {
        return carDescribe;
    }
    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }
    public void setAddrUrl(String addrUrl) 
    {
        this.addrUrl = addrUrl;
    }

    public String getAddrUrl() 
    {
        return addrUrl;
    }
    public void setCarGps(String carGps) 
    {
        this.carGps = carGps;
    }

    public String getCarGps() 
    {
        return carGps;
    }

    public Integer getCarStatus() {
        return carStatus;
    }

    public void setCarStatus(Integer carStatus) {
        this.carStatus = carStatus;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("carCode", getCarCode())
            .append("carModel", getCarModel())
            .append("carPlateNumber", getCarPlateNumber())
            .append("carDescribe", getCarDescribe())
            .append("deptId", getDeptId())
            .append("addrUrl", getAddrUrl())
            .append("carGps", getCarGps())
            .append("carStatus", getCarStatus())
            .append("lon", getLon())
            .append("lat", getLat())
            .toString();
    }
}
