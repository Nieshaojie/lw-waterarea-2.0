package com.mskyeye.trace.model;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @ClassName:LwAiAlarmPacket
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/22 10:46
 * @Version:1.0
 **/
@Data
public class LwAiAlarmPacket {

    @JSONField(name = "CMDTYPE")
    private String CMDTYPE;//告警标识,包括AI识别告警、海康识别告警

    @JSONField(name = "CAMERAID")
    private Long CAMERAID;

    @JSONField(name = "CAMERANAME")
    private String CAMERANAME;

    @JSONField(name = "DEPTID")
    private Long DEPTID;

    @JSONField(name = "EVENTTYPE")
    private Long EVENTTYPE;

    @JSONField(name = "LON")
    private Double LON;

    @JSONField(name = "LAT")
    private Double LAT;

    @JSONField(name = "AIPOINTNAME")
    private String AIPOINTNAME;
}
