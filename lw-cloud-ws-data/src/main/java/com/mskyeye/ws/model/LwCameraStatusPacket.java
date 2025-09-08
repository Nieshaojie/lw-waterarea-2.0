package com.mskyeye.ws.model;

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
    /** 相机厂家 */
    @JSONField(name = "MANU")
    private String MANU;

    @JSONField(name = "TRALAT")
    private Double TRALAT;

    @JSONField(name = "TRALON")
    private Double TRALON;

    public Double getTRALAT() {
        return TRALAT;
    }

    public void setTRALAT(Double TRALAT) {
        this.TRALAT = TRALAT;
    }

    public Double getTRALON() {
        return TRALON;
    }

    public void setTRALON(Double TRALON) {
        this.TRALON = TRALON;
    }

    public String getMANU() {return MANU;}

    public void setMANU(String MANU) {this.MANU = MANU;}

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
}
