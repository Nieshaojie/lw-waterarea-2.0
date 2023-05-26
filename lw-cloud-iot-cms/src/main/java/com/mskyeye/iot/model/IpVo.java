package com.mskyeye.iot.model;
import lombok.Data;
/**
 * @ClassName:IpVo
 * @Description:ip
 * @Author:R.Gong
 * @Date:2022/12/2 18:06
 * @Version:1.0
 **/
@Data
public class IpVo {
    private String ip;//IP地址
    private String pro;//省
    private String proCode;//省编码
    private String city;//城市
    private String cityCode;//城市编码
    private String region;//区
    private String regionCode;//区编码
    private String addr;//详细地址 + 运营商

    //主要用于接参，无实际意义
    private String regionNames;
    private String err;

    @Override
    public String toString() {
        return "ip='" + ip + '\'' +
                ", 省='" + pro + '\'' +
                ", 省编码='" + proCode + '\'' +
                ", 城市='" + city + '\'' +
                ", 城市编码='" + cityCode + '\'' +
                ", 区='" + region + '\'' +
                ", 区编码='" + regionCode + '\'' +
                ", 详细地址+运营商='" + addr + '\'';
    }
}

