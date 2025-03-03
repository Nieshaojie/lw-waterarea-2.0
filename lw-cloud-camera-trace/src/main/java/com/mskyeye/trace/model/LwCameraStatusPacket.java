package com.mskyeye.trace.model;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * @ClassName:LwCameraStatusPacket
 * @Description:瞭望相机状态数据包
 * @Author:R.Gong
 * @Date:2023/8/9 11:41
 * @Version:1.0
 **/
public class LwCameraStatusPacket {

    @JSONField(name = "CMDTYPE")
    private String CMDTYPE = "CAMERASTATUS";//相机状态标识

    @JSONField(name = "IPCID")
    private Long IPCID;

    @JSONField(name = "IPCLAT")
    private Double IPCLAT;

    @JSONField(name = "IPCLON")
    private Double IPCLON;

    @JSONField(name = "IPCNAME")
    private String IPCNAME;

    @JSONField(name = "IPCSTATUS")
    private String IPCSTATUS;

    @JSONField(name = "PVAL")
    private Double PVAL;//光电方位P值

    @JSONField(name = "TVAL")
    private Double TVAL;//光电方位T值

    @JSONField(name = "ZVAL")
    private Double ZVAL;//光电方位Z值

    @JSONField(name = "ORIPVAL")
    private Double ORIPVAL;//未纠正的原始P值

    @JSONField(name = "ISAVALARM")
    private Integer ISAVALARM = 0;//是否为声光警戒设备

    @JSONField(name = "ISOPENLIGHT")
    private Integer ISOPENLIGHT = 0;//是否已打开光源

    public String getCMDTYPE() {
        return CMDTYPE;
    }

    public void setCMDTYPE(String CMDTYPE) {
        this.CMDTYPE = CMDTYPE;
    }

    public Long getIPCID() {
        return IPCID;
    }

    public void setIPCID(Long IPCID) {
        this.IPCID = IPCID;
    }

    public Double getIPCLAT() {
        return IPCLAT;
    }

    public void setIPCLAT(Double IPCLAT) {
        this.IPCLAT = IPCLAT;
    }

    public Double getIPCLON() {
        return IPCLON;
    }

    public void setIPCLON(Double IPCLON) {
        this.IPCLON = IPCLON;
    }

    public String getIPCSTATUS() {
        return IPCSTATUS;
    }

    public void setIPCSTATUS(String IPCSTATUS) {
        this.IPCSTATUS = IPCSTATUS;
    }

    public Double getPVAL() {
        return PVAL;
    }

    public void setPVAL(Double PVAL) {
        this.PVAL = PVAL;
    }

    public Double getTVAL() {
        return TVAL;
    }

    public void setTVAL(Double TVAL) {
        this.TVAL = TVAL;
    }

    public Double getZVAL() {
        return ZVAL;
    }

    public void setZVAL(Double ZVAL) {
        this.ZVAL = ZVAL;
    }

    public String getIPCNAME() {
        return IPCNAME;
    }

    public void setIPCNAME(String IPCNAME) {
        this.IPCNAME = IPCNAME;
    }

    public Double getORIPVAL() {
        return ORIPVAL;
    }

    public void setORIPVAL(Double ORIPVAL) {
        this.ORIPVAL = ORIPVAL;
    }

    public Integer getISAVALARM() {
        return ISAVALARM;
    }

    public void setISAVALARM(Integer ISAVALARM) {
        this.ISAVALARM = ISAVALARM;
    }

    public Integer getISOPENLIGHT() {
        return ISOPENLIGHT;
    }

    public void setISOPENLIGHT(Integer ISOPENLIGHT) {
        this.ISOPENLIGHT = ISOPENLIGHT;
    }
}
