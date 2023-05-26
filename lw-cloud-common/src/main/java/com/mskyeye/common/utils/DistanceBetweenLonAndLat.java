package com.mskyeye.common.utils;

/**
 * @ClassName:DistanceBetweenLonAndLat
 * @Description:根据经纬度计算方位和距离
 * @Author:R.Gong
 * @Date:2021/1/18 14:55
 * @Version:1.0
 **/
public class DistanceBetweenLonAndLat {

    private static final double M_PI = 3.14159265358979323846;

    private static final double EARTH_RADIUS = 6371.0;

    public double HaverSin(double thera) {
        double v = Math.sin(thera / 2);
        return v * v;
    }

    //角度转换为弧度
    public double ConvertDegreesToRadians(double degrees) {
        return degrees * M_PI / 180;
    }

    //弧度转换为角度
    public double ConvertRadiansToDegrees(double radian) {
        return radian * 180.0 / M_PI;
    }

    //计算两个经纬度坐标间的距离(单位：米)
    //参数：lon1为经度1；lat1为纬度1；lon2为经度2；lat2为纬度2
    public double get_distance(double lon1, double lat1, double lon2, double lat2) {
        lat1 = ConvertDegreesToRadians(lat1);
        lon1 = ConvertDegreesToRadians(lon1);
        lat2 = ConvertDegreesToRadians(lat2);
        lon2 = ConvertDegreesToRadians(lon2);

        double vLon = Math.abs(lon1 - lon2);
        double vLat = Math.abs(lat1 - lat2);
        double h = HaverSin(vLat) + Math.cos(lat1) * Math.cos(lat2) * HaverSin(vLon);
        double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));

        return distance * 1000;
    }

    //两个经纬度间连线与正北方向的夹角
    //参数：lon1为经度1；lat1为纬度1；lon2为经度2；lat2为纬度
    public int get_angle(double lon1, double lat1, double lon2, double lat2) {
        double x = lat1 - lat2;
        double y = lon1 - lon2;
        int angle = -1;
        if (y == 0 && x > 0)
            angle = 0;
        if (y == 0 && x < 0)
            angle = 180;
        if (x == 0 && y > 0)
            angle = 90;
        if (x == 0 && y < 0)
            angle = 270;
        if (angle == -1)
        {
            double dislon = get_distance(lon1, lat2, lon2, lat2);
            double dislat = get_distance(lon2, lat1, lon2, lat2);
            if (x > 0 && y > 0)
                angle = (int) (Math.atan2(dislon, dislat) / M_PI * 180);
            if (x < 0 && y > 0)
                angle = (int) (Math.atan2(dislat, dislon) / M_PI * 180 + 90);
            if (x < 0 && y < 0)
                angle = (int) (Math.atan2(dislon, dislat) / M_PI * 180 + 180);
            if (x > 0 && y < 0)
                angle = (int) (Math.atan2(dislat, dislon) / M_PI * 180 + 270);
        }
        return angle;

    }

    //得到方向
    public String get_direction(double lon1, double lat1, double lon2, double lat2){
        int angle = get_angle( lon1,  lat1,  lon2,  lat2);
        String direction = "";
        switch (angle){
            case 0:direction = "正北";break;
            case 90:direction = "正东";break;
            case 180:direction = "正南";break;
            case 270:direction = "正西";break;
            default:
                if (angle > 0 && angle < 90) {
                    direction = "东北";
                }else if(angle > 90 && angle < 180){
                    direction = "东南";
                }else if(angle > 180 && angle < 270){
                    direction = "西南";
                }else if(angle > 270 && angle < 360){
                    direction = "西北";
                }
                break;
        }
        return direction;
    }

    //将角度限制在[0,360),0--北，90--东，180--南，270--西；与指南针一致
    public int AngleSpecification(int angle) {
        int curAngle;
        if (angle < 0)
        {
            curAngle = (angle + 360) % 360;
        }
        else {
            curAngle = angle % 360;
        }
        return curAngle;
    }

}
