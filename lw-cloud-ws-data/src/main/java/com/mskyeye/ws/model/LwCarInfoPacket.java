package com.mskyeye.ws.model;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @ClassName:LwCarInfoPacket
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/11/14 13:18
 * @Version:1.0
 **/
@Data
public class LwCarInfoPacket {

    @JSONField(name = "CMDTYPE")
    private String CMDTYPE = "CARINFO";//信息标识

    @JSONField(name = "CARCODE")
    private String CARCODE;

    @JSONField(name = "CARGPS")
    private String CARGPS;

    @JSONField(name = "DEPTID")
    private Long DEPTID;

    @JSONField(name = "PHOTOURL")
    private String PHOTOURL;

    @JSONField(name = "LON")
    private Double LON;

    @JSONField(name = "LAT")
    private Double LAT;

    @JSONField(name = "SHOWINFO")
    private String SHOWINFO;
}
