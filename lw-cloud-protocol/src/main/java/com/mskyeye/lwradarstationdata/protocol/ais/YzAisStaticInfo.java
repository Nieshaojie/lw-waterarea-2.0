package com.mskyeye.lwradarstationdata.protocol.ais;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AIS静态信息对象 yz_ais_static_info
 *
 * @author ruoyi
 * @date 2023-05-25
 */
@Data
public class YzAisStaticInfo implements Serializable
{
    /** mmsi */
    private Long mmsi;

    /** imo */
    private Long imo;

    /** 呼号 */
    private String callSign;

    /** 船名 */
    private String shipName;

    /** 船舶类型 */
    private String shipType;

    /** 吃水深度 */
    private BigDecimal draught;

}

