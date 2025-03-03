package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:PatrolData
 * @Description:巡护数据
 * @Author:R.Gong
 * @Date:2024/5/30 14:24
 * @Version:1.0
 **/
@Data
public class PatrolData {

    private PatrolUserInfo patrolUserInfo;
    private String latitude;
    private String longitude;
    private String online;
    private String name;
    private String time;
    private String stationId = "0";
}
