package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:DH_PTZ_LOCATION_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 11:31
 * @Version:1.0
 **/
public class DH_PTZ_LOCATION_INFO extends SdkStructure {

//    public int nChannelID;
//    public int nPTZPan;
//    public int nPTZTilt;
//    public int nPTZZoom;
//    public byte bState;
//    public byte bAction;
//    public byte bFocusState;
//    public byte bEffectiveInTimeSection;
//    public int nPtzActionID;
//    public int dwPresetID;
//    public float fFocusPosition;
//    public byte bZoomState;
//    public byte[] bReserved = new byte[3];
//    public int dwSequence;
//    public int dwUTC;
//    public int emPresetStatus;
//    public int nZoomValue;
//    public NET_PTZSPACE_UNNORMALIZED stuAbsPosition;
//    public int nFocusMapValue;
//    public int nZoomMapValue;
//    public int emPanTiltStatus;
//    public NET_EVENT_INFO_EXTEND stuEventInfoEx;
//    public byte[] reserved = new byte[696];

    public int nChannelID;
    public int nPTZPan;
    public int nPTZTilt;
    public int nPTZZoom;
    public byte bState;
    public byte bAction;
    public byte bFocusState;
    public byte bEffectiveInTimeSection;
    public int nPtzActionID;
    public int dwPresetID;
    public float fFocusPosition;
    public byte bZoomState;
    public byte[] bReserved = new byte[3];
    public int dwSequence;
    public int dwUTC;
    public int emPresetStatus;
    public int[] reserved = new int[248];
}
