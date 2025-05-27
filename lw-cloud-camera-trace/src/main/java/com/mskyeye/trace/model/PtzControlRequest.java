package com.mskyeye.trace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PtzControlRequest {
    private String msg = "ptz_ctrl";
    private int camid;
    private int action;
    private int speed; // 0 - 100
    //1:速度自适应打开 0:关闭
    private int speedAutoFit; // 0 or 1
}
