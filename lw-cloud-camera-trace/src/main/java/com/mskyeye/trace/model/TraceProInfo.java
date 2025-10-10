package com.mskyeye.trace.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * @ClassName:TraceProInfo
 * @Description:跟踪协议信息
 * @Author:R.Gong
 * @Date:2023/8/10 19:18
 * @Version:1.0
 **/
public class TraceProInfo {

    private Integer traceType;//跟踪类型: 0:无跟踪 1:联动跟踪 2:框选跟踪 3:图像跟踪 4:光电引导 5:AI巡航 6:雷光警戒 7、警戒抓拍
    private Boolean bTracking = true;//是否跟踪
    private Long targetId;//跟踪目标ID,-1为非目标的跟踪指令
    private Double traceLon;//跟踪纬度
    private Double traceLat;//跟踪纬度
    private Double traceAlt;//跟踪高度
    private Long cameraId;//相机ID
    private String manu;//相机厂家
    private Integer channelId = 1;//1:可见光 2:热成像
    private Double left = 0.0;
    private Double top = 0.0;
    private Double right = 0.0;
    private Double bottom = 0.0;
    private Long orderTime = System.currentTimeMillis();//发送指令时间

    private Integer rotaDir = -1;//转动方向：-1 初始值 0 左 1 右

    private Boolean bStopRota = true;//是否停止转动

    private Integer clock = 0;//计时器,用于雷光警戒

    private String alarmAbsUrl;//雷光警戒抓拍视频绝对地址
    private String alarmRelUrl;//雷光警戒抓拍视频相对地址，用于存储数据库

    public Double getTraceAlt() {
        return traceAlt;
    }

    public void setTraceAlt(Double traceAlt) {
        this.traceAlt = traceAlt;
    }

    public Integer getTraceType() {
        return traceType;
    }

    public void setTraceType(Integer traceType) {
        this.traceType = traceType;
    }

    public Boolean getbTracking() {
        return bTracking;
    }

    public void setbTracking(Boolean bTracking) {
        this.bTracking = bTracking;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    public Double getLeft() {
        return left;
    }

    public void setLeft(Double left) {
        this.left = left;
    }

    public Double getTop() {
        return top;
    }

    public void setTop(Double top) {
        this.top = top;
    }

    public Double getRight() {
        return right;
    }

    public void setRight(Double right) {
        this.right = right;
    }

    public Double getBottom() {
        return bottom;
    }

    public void setBottom(Double bottom) {
        this.bottom = bottom;
    }

    public Long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Long orderTime) {
        this.orderTime = orderTime;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getManu() {
        return manu;
    }

    public void setManu(String manu) {
        this.manu = manu;
    }

    public Double getTraceLon() {
        return traceLon;
    }

    public void setTraceLon(Double traceLon) {
        this.traceLon = traceLon;
    }

    public Double getTraceLat() {
        return traceLat;
    }

    public void setTraceLat(Double traceLat) {
        this.traceLat = traceLat;
    }

    public Integer getRotaDir() {
        return rotaDir;
    }

    public void setRotaDir(Integer rotaDir) {
        this.rotaDir = rotaDir;
    }

    public Boolean getbStopRota() {
        return bStopRota;
    }

    public void setbStopRota(Boolean bStopRota) {
        this.bStopRota = bStopRota;
    }

    public Integer getClock() {
        return clock;
    }

    public void setClock(Integer clock) {
        this.clock = clock;
    }

    public String getAlarmAbsUrl() {
        return alarmAbsUrl;
    }

    public void setAlarmAbsUrl(String alarmAbsUrl) {
        this.alarmAbsUrl = alarmAbsUrl;
    }

    public String getAlarmRelUrl() {
        return alarmRelUrl;
    }

    public void setAlarmRelUrl(String alarmRelUrl) {
        this.alarmRelUrl = alarmRelUrl;
    }
}
