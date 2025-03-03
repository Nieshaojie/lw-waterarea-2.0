package com.mskyeye.handler.model;

/**
 * @ClassName:LonLatInfo
 * @Description:经纬度信息
 * @Author:R.Gong
 * @Date:2023/7/19 14:55
 * @Version:1.0
 **/
public class LonLatInfo {

    /**经度 */
    private double lon;

    /**纬度 */
    private double lat;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "LonLatInfo{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
