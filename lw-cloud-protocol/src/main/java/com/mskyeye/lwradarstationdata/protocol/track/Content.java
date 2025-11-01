package com.mskyeye.lwradarstationdata.protocol.track;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.LonLatDoubleSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.OyzFloatSerialize;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName:Content
 * @Description:航迹信息内容
 * @Author:R.Gong
 * @Date:2023/7/11 10:18
 * @Version:1.0
 **/
public class Content implements Serializable {

    @JSONField(name = "SOURCE")
    private Integer SOURCE;//数据源类型：0：雷达目标；1：AIS目标；2：雷达和AIS融合目标 3：正向外推AIS,4：反向外推AIS,5:北斗目标 6：反无雷达

    @JSONField(name = "STATIONID")
    private Integer STATIONID;//探测站ID

    @JSONField(name = "ALARM")
    private String ALARM;//预警类型名称

    @JSONField(name = "TID")
    private Long TID;//目标批号

    @JSONField(name = "STATUS")
    private Integer STATUS;//0为删除该目标,1为新增、更新目标

    @JSONField(name = "MMSI")
    private Long MMSI;//MMSI

//    @JSONField(name = "NAME")
//    private String NAME;//船名

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
    private Float SPEED;//航速，单位：m/s  （*1.944转换成节）
    @JsonSerialize(using = OyzFloatSerialize.class)
    @JSONField(name = "HEAD")
    private Float HEAD;//船首向

    @JSONField(name = "NAVIGATION")
    private Integer NAVIGATION = 1;//0:“低速” 1:“在航”
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

    @JSONField(name = "imo")
    private Long IMO;

    @JSONField(name = "CALLSIGN")
    private String CALLSIGN;

    @JSONField(name = "SHIPNAME")
    private String SHIPNAME;

    @JSONField(name = "SHIPTYPE")
    private String SHIPTYPE;

    @JSONField(name = "DRAUGHT")
    private BigDecimal DRAUGHT;

    @JSONField(name = "COUNTRY")
    private String COUNTRY = "未知";

    @JSONField(name = "SHIPLENGTH")
    private Integer SHIPLENGTH;

    @JSONField(name = "SHIPWIDTH")
    private Integer SHIPWIDTH;

    @JSONField(name = "FLYTYPE")
    private Integer FLYTYPE;

    @JSONField(name = "ALT")
    private Double ALT;

    @JSONField(name = "SN")
    private String SN;

    public String getSN() {
        return SN;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }

    public Double getALT() {
        return ALT;
    }

    public void setALT(Double ALT) {
        this.ALT = ALT;
    }

    public Integer getFLYTYPE() {
        return FLYTYPE;
    }

    public void setFLYTYPE(Integer FLYTYPE) {
        this.FLYTYPE = FLYTYPE;
    }

    public Integer getSOURCE() {
        return SOURCE;
    }

    public void setSOURCE(Integer SOURCE) {
        this.SOURCE = SOURCE;
    }

    public Integer getSTATIONID() {
        return STATIONID;
    }

    public void setSTATIONID(Integer STATIONID) {
        this.STATIONID = STATIONID;
    }

    public String getALARM() {
        return ALARM;
    }

    public void setALARM(String ALARM) {
        this.ALARM = ALARM;
    }

    public Long getTID() {
        return TID;
    }

    public void setTID(Long TID) {
        this.TID = TID;
    }

    public Integer getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(Integer STATUS) {
        this.STATUS = STATUS;
    }

    public Long getMMSI() {
        return MMSI;
    }

    public void setMMSI(Long MMSI) {
        this.MMSI = MMSI;
    }

    public Double getLAT() {
        return LAT;
    }

    public void setLAT(Double LAT) {
        this.LAT = LAT;
    }

    public Double getLON() {
        return LON;
    }

    public void setLON(Double LON) {
        this.LON = LON;
    }

    public Float getCOURSE() {
        return COURSE;
    }

    public void setCOURSE(Float COURSE) {
        this.COURSE = COURSE;
    }

    public Float getSPEED() {
        return SPEED;
    }

    public void setSPEED(Float SPEED) {
        this.SPEED = SPEED;
    }

    public Float getHEAD() {
        return HEAD;
    }

    public void setHEAD(Float HEAD) {
        this.HEAD = HEAD;
    }

    public Integer getNAVIGATION() {
        return NAVIGATION;
    }

    public void setNAVIGATION(Integer NAVIGATION) {
        this.NAVIGATION = NAVIGATION;
    }

    public Float getRANGEMETRES() {
        return RANGEMETRES;
    }

    public void setRANGEMETRES(Float RANGEMETRES) {
        this.RANGEMETRES = RANGEMETRES;
    }

    public Float getAZIMUTHDEGREES() {
        return AZIMUTHDEGREES;
    }

    public void setAZIMUTHDEGREES(Float AZIMUTHDEGREES) {
        this.AZIMUTHDEGREES = AZIMUTHDEGREES;
    }

    public Float getSIZEMETRES() {
        return SIZEMETRES;
    }

    public void setSIZEMETRES(Float SIZEMETRES) {
        this.SIZEMETRES = SIZEMETRES;
    }

    public Float getSIZEDEGREES() {
        return SIZEDEGREES;
    }

    public void setSIZEDEGREES(Float SIZEDEGREES) {
        this.SIZEDEGREES = SIZEDEGREES;
    }

    public Integer getWEIGHT() {
        return WEIGHT;
    }

    public void setWEIGHT(Integer WEIGHT) {
        this.WEIGHT = WEIGHT;
    }

    public Integer getSTRENGTH() {
        return STRENGTH;
    }

    public void setSTRENGTH(Integer STRENGTH) {
        this.STRENGTH = STRENGTH;
    }

    public Long getIMO() {
        return IMO;
    }

    public void setIMO(Long IMO) {
        this.IMO = IMO;
    }

    public String getCALLSIGN() {
        return CALLSIGN;
    }

    public void setCALLSIGN(String CALLSIGN) {
        this.CALLSIGN = CALLSIGN;
    }

    public String getSHIPNAME() {
        return SHIPNAME;
    }

    public void setSHIPNAME(String SHIPNAME) {
        this.SHIPNAME = SHIPNAME;
    }

    public String getSHIPTYPE() {
        return SHIPTYPE;
    }

    public void setSHIPTYPE(String SHIPTYPE) {
        this.SHIPTYPE = SHIPTYPE;
    }

    public BigDecimal getDRAUGHT() {
        return DRAUGHT;
    }

    public void setDRAUGHT(BigDecimal DRAUGHT) {
        this.DRAUGHT = DRAUGHT;
    }

    public String getCOUNTRY() {
        return COUNTRY;
    }

    public void setCOUNTRY(String COUNTRY) {
        this.COUNTRY = COUNTRY;
    }

    public Integer getSHIPLENGTH() {
        return SHIPLENGTH;
    }

    public void setSHIPLENGTH(Integer SHIPLENGTH) {
        this.SHIPLENGTH = SHIPLENGTH;
    }

    public Integer getSHIPWIDTH() {
        return SHIPWIDTH;
    }

    public void setSHIPWIDTH(Integer SHIPWIDTH) {
        this.SHIPWIDTH = SHIPWIDTH;
    }
}
