package com.mskyeye.trace.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mskyeye.trace.camera.dhkj.sdk.DhNetSDK;
import com.mskyeye.trace.camera.gpl.sdk.GplNetSDK;
import com.mskyeye.trace.camera.hkws.sdk.HkNetSDK;
import com.mskyeye.trace.netty.control.GplCtrlTcpClientService;
import com.mskyeye.trace.netty.status.GplStatusTcpClientService;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * camera对象 yz_camera_info
 * 
 * @author R.Gong
 * @date 2023-04-13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YzCameraInfo
{

    /** id */
    private Long id;

    /** 相机名称 */
    private String name;

    /** 可见光编号 */
    private String lightCode;

    /** 热成像编号 */
    private String thermalCode;

    /** 经度 */
    private BigDecimal lat;

    /** 纬度 */
    private BigDecimal lon;

    /** ip */
    private String ip;

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String passWord;

    /** 部门id */
    private Long deptId;


    /** 相机http端口 */
    private Long httpPort;

    /** 相机管理端口 */
    private Long manPort;

    /** 相机厂家 */
    private String manu;


    /** 报警联动 */
    private String linkFlag;

    /** 高度 */
    private Double height = 20.0;

    /** 修正角度 */
    private Double angle = 0.0;

    /** 修正P值 */
    private Double pVal = 0.0;

    /** 修正T值 */
    private Double tVal = 0.0;

    /** 修正Z值 */
    private Double zVal = 0.0;

    /** 当前P值 */
    private Double curPVal = 0.0;

    /** 当前T值 */
    private Double curTVal = 0.0;

    /** 当前Z值 */
    private Double curZVal = 0.0;

    /** 原始P值 */
    private Double oriPVal = 0.0;
    /** 登录信息 */
    private String loginInfo = "-1";//海康大华是登录ID,和普是登录token

    private HkNetSDK hkNetSDK;//海康相机对象

    private DhNetSDK dhNetSDK;//大华相机对象

    private GplNetSDK gplNetSDK;//高普乐相机对象

    private Integer traceType = 0;//跟踪类型 0:未跟踪 1:联动跟踪 2:框选跟踪 3:图像跟踪

    private Integer lHandle = -1;//布防返回值(海康独有，用于来安船舶告警)

    /** 备用端口1 */
    private Long sparePort1;

    /** 备用端口2 */
    private Long sparePort2;

    /** 高普乐相机方位倍率 */
    private Double aziMultiply;

    /** 高普乐相机俯仰倍率 */
    private Double pitchMultiply;

    /** 高普乐相机方位零点值 */
    private Integer aziZeroVal;

    /** 高普乐相机俯仰零点值 */
    private Integer pitchZeroVal;

    /** 高普乐相机方位最小值 */
    private Integer aziMinVal;

    /** 高普乐相机俯仰最小值 */
    private Integer pitchMinVal;

    /** 高普乐相机控制口对象 **/
    private GplCtrlTcpClientService gplCtrlTcpClient;

    /** 高普乐相机状态口TCP对象 **/
    private GplStatusTcpClientService gplStatusTcpClient;

    /** 可见光RTSP地址 */
    private String lightRtsp;

    /** 热成像RTSP地址 */
    private String thermalRtsp;

    /** 是否为声光警戒设备 */
    private Integer isAvAlarm = 0;//0:否 1:是

    /** 是否开启强光 */
    private Integer isLightOpen;//0:否 1:是

    private volatile boolean rcAlarmPaused = false;

    public boolean isRcAlarmPaused() {
        return rcAlarmPaused;
    }

    public void setRcAlarmPaused(boolean rcAlarmPaused) {
        this.rcAlarmPaused = rcAlarmPaused;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLightCode() {
        return lightCode;
    }

    public void setLightCode(String lightCode) {
        this.lightCode = lightCode;
    }

    public String getThermalCode() {
        return thermalCode;
    }

    public void setThermalCode(String thermalCode) {
        this.thermalCode = thermalCode;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Long httpPort) {
        this.httpPort = httpPort;
    }

    public Long getManPort() {
        return manPort;
    }

    public void setManPort(Long manPort) {
        this.manPort = manPort;
    }

    public String getManu() {
        return manu;
    }

    public void setManu(String manu) {
        this.manu = manu;
    }

    public String getLinkFlag() {
        return linkFlag;
    }

    public void setLinkFlag(String linkFlag) {
        this.linkFlag = linkFlag;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }

    public Double getpVal() {
        return pVal;
    }

    public void setpVal(Double pVal) {
        this.pVal = pVal;
    }

    public Double gettVal() {
        return tVal;
    }

    public void settVal(Double tVal) {
        this.tVal = tVal;
    }

    public Double getzVal() {
        return zVal;
    }

    public void setzVal(Double zVal) {
        this.zVal = zVal;
    }

    public Double getCurPVal() {
        return curPVal;
    }

    public void setCurPVal(Double curPVal) {
        this.curPVal = curPVal;
    }

    public Double getCurTVal() {
        return curTVal;
    }

    public void setCurTVal(Double curTVal) {
        this.curTVal = curTVal;
    }

    public Double getCurZVal() {
        return curZVal;
    }

    public void setCurZVal(Double curZVal) {
        this.curZVal = curZVal;
    }

    public Double getOriPVal() {
        return oriPVal;
    }

    public void setOriPVal(Double oriPVal) {
        this.oriPVal = oriPVal;
    }

    public String getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(String loginInfo) {
        this.loginInfo = loginInfo;
    }

    public HkNetSDK getHkNetSDK() {
        return hkNetSDK;
    }

    public void setHkNetSDK(HkNetSDK hkNetSDK) {
        this.hkNetSDK = hkNetSDK;
    }

    public DhNetSDK getDhNetSDK() {
        return dhNetSDK;
    }

    public void setDhNetSDK(DhNetSDK dhNetSDK) {
        this.dhNetSDK = dhNetSDK;
    }

    public GplNetSDK getGplNetSDK() {
        return gplNetSDK;
    }

    public void setGplNetSDK(GplNetSDK gplNetSDK) {
        this.gplNetSDK = gplNetSDK;
    }

    public Integer getTraceType() {
        return traceType;
    }

    public void setTraceType(Integer traceType) {
        this.traceType = traceType;
    }

    public Integer getlHandle() {
        return lHandle;
    }

    public void setlHandle(Integer lHandle) {
        this.lHandle = lHandle;
    }

    public Long getSparePort1() {
        return sparePort1;
    }

    public void setSparePort1(Long sparePort1) {
        this.sparePort1 = sparePort1;
    }

    public Long getSparePort2() {
        return sparePort2;
    }

    public void setSparePort2(Long sparePort2) {
        this.sparePort2 = sparePort2;
    }

    public Double getAziMultiply() {
        return aziMultiply;
    }

    public void setAziMultiply(Double aziMultiply) {
        this.aziMultiply = aziMultiply;
    }

    public Double getPitchMultiply() {
        return pitchMultiply;
    }

    public void setPitchMultiply(Double pitchMultiply) {
        this.pitchMultiply = pitchMultiply;
    }

    public Integer getAziZeroVal() {
        return aziZeroVal;
    }

    public void setAziZeroVal(Integer aziZeroVal) {
        this.aziZeroVal = aziZeroVal;
    }

    public Integer getPitchZeroVal() {
        return pitchZeroVal;
    }

    public void setPitchZeroVal(Integer pitchZeroVal) {
        this.pitchZeroVal = pitchZeroVal;
    }

    public GplCtrlTcpClientService getGplCtrlTcpClient() {
        return gplCtrlTcpClient;
    }

    public void setGplCtrlTcpClient(GplCtrlTcpClientService gplCtrlTcpClient) {
        this.gplCtrlTcpClient = gplCtrlTcpClient;
    }

    public GplStatusTcpClientService getGplStatusTcpClient() {
        return gplStatusTcpClient;
    }

    public void setGplStatusTcpClient(GplStatusTcpClientService gplStatusTcpClient) {
        this.gplStatusTcpClient = gplStatusTcpClient;
    }

    public String getLightRtsp() {
        return lightRtsp;
    }

    public void setLightRtsp(String lightRtsp) {
        this.lightRtsp = lightRtsp;
    }

    public String getThermalRtsp() {
        return thermalRtsp;
    }

    public void setThermalRtsp(String thermalRtsp) {
        this.thermalRtsp = thermalRtsp;
    }

    public Integer getIsAvAlarm() {
        return isAvAlarm;
    }

    public void setIsAvAlarm(Integer isAvAlarm) {
        this.isAvAlarm = isAvAlarm;
    }

    public Integer getIsLightOpen() {
        return isLightOpen;
    }

    public void setIsLightOpen(Integer isLightOpen) {
        this.isLightOpen = isLightOpen;
    }

    public Integer getAziMinVal() {
        return aziMinVal;
    }

    public void setAziMinVal(Integer aziMinVal) {
        this.aziMinVal = aziMinVal;
    }

    public Integer getPitchMinVal() {
        return pitchMinVal;
    }

    public void setPitchMinVal(Integer pitchMinVal) {
        this.pitchMinVal = pitchMinVal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        YzCameraInfo other = (YzCameraInfo) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(name, other.name) &&
                Objects.equals(lightCode, other.lightCode) &&
                Objects.equals(thermalCode, other.thermalCode) &&
                Objects.equals(lat, other.lat) &&
                Objects.equals(lon, other.lon) &&
                Objects.equals(ip, other.ip) &&
                Objects.equals(userName, other.userName) &&
                Objects.equals(passWord, other.passWord) &&
                Objects.equals(deptId, other.deptId) &&
                Objects.equals(httpPort, other.httpPort) &&
                Objects.equals(manPort, other.manPort) &&
                Objects.equals(manu, other.manu) &&
                Objects.equals(height, other.height) &&
                Objects.equals(angle, other.angle) &&
                Objects.equals(pVal, other.pVal) &&
                Objects.equals(tVal, other.tVal) &&
                Objects.equals(zVal, other.zVal) &&
                Objects.equals(sparePort1, other.sparePort1) &&
                Objects.equals(sparePort2, other.sparePort2) &&
                Objects.equals(aziMultiply, other.aziMultiply) &&
                Objects.equals(pitchMultiply, other.pitchMultiply) &&
                Objects.equals(aziZeroVal, other.aziZeroVal) &&
                Objects.equals(pitchZeroVal, other.pitchZeroVal) &&
                Objects.equals(aziMinVal, other.aziMinVal) &&
                Objects.equals(pitchMinVal, other.pitchMinVal) &&
                Objects.equals(lightRtsp, other.lightRtsp) &&
                Objects.equals(thermalRtsp, other.thermalRtsp);
    }

    @Override
    public String toString() {
        return "YzCameraInfo{" +
                "id="+id+
                "name="+name+
                ",loginInfo=" + loginInfo +
                ",curPVal=" + curPVal +
                ", curTVal=" + curTVal +
                ", curZVal=" + curZVal +
                ", manu=" + manu +
                ", name=" + name +
                ", 修正角度=" + angle +
                ", 修正P=" + pVal +
                ", 修正T=" + tVal +
                ", 修正Z=" + zVal +
                '}';
    }
}
