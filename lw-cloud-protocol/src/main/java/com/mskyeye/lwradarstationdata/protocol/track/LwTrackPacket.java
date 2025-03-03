package com.mskyeye.lwradarstationdata.protocol.track;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:LwTrackPacket
 * @Description:瞭望航迹数据
 * @Author:R.Gong
 * @Date:2022/12/6 15:15
 * @Version:1.0
 **/
public class LwTrackPacket implements Serializable {

    @JSONField(name = "CMDTYPE")
    private String CMDTYPE = "TARGETTRACK";//航迹标识

    @JSONField(name = "NUM")
    private Integer NUM = 1;

    @JSONField(name = "ITEM")
    private List<Content> ITEM = new ArrayList<>();

    @JSONField(name = "TIME")
    private long TIME;//航迹时间

    public String getCMDTYPE() {
        return CMDTYPE;
    }

    public void setCMDTYPE(String CMDTYPE) {
        this.CMDTYPE = CMDTYPE;
    }

    public Integer getNUM() {
        return NUM;
    }

    public void setNUM(Integer NUM) {
        this.NUM = NUM;
    }

    public List<Content> getITEM() {
        return ITEM;
    }

    public void setITEM(List<Content> ITEM) {
        this.ITEM = ITEM;
    }

    public long getTIME() {
        return TIME;
    }

    public void setTIME(long TIME) {
        this.TIME = TIME;
    }

    @Override
    public String toString() {
        return "LwTrackPacket{" +
                "CMDTYPE='" + CMDTYPE + '\'' +
                ", NUM=" + NUM +
                ", ITEM=" + ITEM +
                ", TIME=" + TIME +
                '}';
    }
}
