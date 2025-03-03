package com.mskyeye.trace.model;

import lombok.Data;

/**
 * @ClassName:CameraOrder
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/6/4 10:03
 * @Version:1.0
 **/
@Data
public class CameraOrder {

    /** id */
    private Long id;
    /** 行为 */
    //1:强光
    private Integer action;

    /** 命令 */
    private Integer command;
}
