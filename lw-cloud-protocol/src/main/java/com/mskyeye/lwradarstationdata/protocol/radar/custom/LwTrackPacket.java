package com.mskyeye.lwradarstationdata.protocol.radar.custom;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.LonLatDoubleSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.OyzFloatSerialize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:LwTrackPacket
 * @Description:瞭望航迹数据
 * @Author:R.Gong
 * @Date:2022/12/6 15:15
 * @Version:1.0
 **/
@Data
public class LwTrackPacket {

    @JSONField(name = "CMDTYPE")
    private String CMDTYPE = "TARGETTRACK";//航迹标识

    @JSONField(name = "NUM")
    private Integer NUM = 1;

    @JSONField(name = "ITEM")
    private List<LwTrackPacket.Content> ITEM = new ArrayList<>();

    @JSONField(name = "TIME")
    private long TIME;//航迹时间

    @Data
    public static class Content{

        @JSONField(name = "SOURCE")
        private Integer SOURCE;//数据源类型：0：雷达目标；1：AIS目标；2：雷达和AIS融合目标

        @JSONField(name = "STATIONID")
        private Integer STATIONID;//探测站ID

        @JSONField(name = "ALARM")
        private Integer ALARM;//是否为报警目标，大于0为报警目标；等于0为非报警目标

        @JSONField(name = "TID")
        private Long TID;//目标批号

        @JSONField(name = "STATUS")
        private Integer STATUS;//1为新增目标，3为删除该目标

        @JSONField(name = "MMSI")
        private Integer MMSI;//MMSI

        @JSONField(name = "NAME")
        private String NAME;//船名

        @JsonSerialize(using = LonLatDoubleSerialize.class)
        @JSONField(name = "LAT")
        private Double LAT;//纬度
        @JsonSerialize(using = LonLatDoubleSerialize.class)
        @JSONField(name = "LON")
        private Double LON;//经度
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "COURSE")
        private Float COURSE;//航向，单位：度
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "SPEED")
        private Float SPEED;//航速，单位：100*m/s  （*1.944/100转换成节）
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "HEAD")
        private Float HEAD;//船首向

        @JSONField(name = "NAVIGATION")
        private Integer NAVIGATION = 20;//20：“在航”1：“锚泊”
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "RANGEMETRES")
        private Float RANGEMETRES;

        @JSONField(name = "AZIMUTHDEGREES")
        private Float AZIMUTHDEGREES;
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "SIZEMETRES")
        private Float SIZEMETRES;
        @JsonSerialize(using = OyzFloatSerialize.class)
        @JSONField(name = "SIZEDEGREES")
        private Float SIZEDEGREES;

        @JSONField(name = "WEIGHT")
        private Integer WEIGHT;

        @JSONField(name = "STRENGTH")
        private Integer STRENGTH;
    }
}
