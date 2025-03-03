package com.mskyeye.trace.model;

import java.util.Date;

/**
 * @ClassName:DeviceMonitorInfoBody
 * @Description:设备监控信息体(水天科技)
 * @Author:R.Gong
 * @Date:2023/8/29 11:51
 * @Version:1.0
 **/
public class DeviceMonitorInfoBody {

    private String CameraId;
    private Integer Type;
    private String Msg;
    private String ObjId;
    private Integer ObjType;
    private RectInfo ObjRect;
    private String ImageUrl;
    private String VideoUrl;
    private Double Lng;
    private Double Lat;
    private Double Height;
    private String ext;
    private Date AlarmTime;

    public String getCameraId() {
        return CameraId;
    }

    public void setCameraId(String cameraId) {
        CameraId = cameraId;
    }

    public Integer getType() {
        return Type;
    }

    public void setType(Integer type) {
        Type = type;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public String getObjId() {
        return ObjId;
    }

    public void setObjId(String objId) {
        ObjId = objId;
    }

    public Integer getObjType() {
        return ObjType;
    }

    public void setObjType(Integer objType) {
        ObjType = objType;
    }

    public RectInfo getObjRect() {
        return ObjRect;
    }

    public void setObjRect(RectInfo objRect) {
        ObjRect = objRect;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return VideoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        VideoUrl = videoUrl;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getHeight() {
        return Height;
    }

    public void setHeight(Double height) {
        Height = height;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Date getAlarmTime() {
        return AlarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        AlarmTime = alarmTime;
    }

    public class RectInfo{
        private double left;
        private double top;
        private double width;
        private double height;

        public double getLeft() {
            return left;
        }

        public void setLeft(double left) {
            this.left = left;
        }

        public double getTop() {
            return top;
        }

        public void setTop(double top) {
            this.top = top;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }


}
