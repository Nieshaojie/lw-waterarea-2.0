package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_SHIPSINFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/29 14:59
 * @Version:1.0
 **/
public class NET_DVR_SHIPSINFO extends SdkStructure {

    public float fShipsLength;
    public float fShipsHeight;
    public float fShipsWidth;
    public float fShipsSpeed;
    public byte byShipsDirection;
    public byte byShipsDetState;
    public byte byTriggerLineID;
    public byte[] byRes = new byte[61];
    public NET_VCA_POLYGON struShipsRect = new NET_VCA_POLYGON();
}
