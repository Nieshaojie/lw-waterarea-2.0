package com.mskyeye.trace.camera.gpl.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;
import com.sun.jna.NativeLong;

/**
 * @ClassName:VS_PTZ_LOCATION_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/10 10:47
 * @Version:1.0
 **/
public class VS_PTZ_LOCATION_INFO extends SdkStructure {
    public int nChannelID;
    public int nPTZPan;
    public int nPTZTilt;
    public int nPTZZoom;
    public byte bState;
    public byte bAction;
    public byte bFocusState;
    public byte bEffectiveInTimeSection;
    public int nPtzActionID;
    public NativeLong dwPresetID; // DWORD in C is typically mapped to NativeLong in Java
    public float fFocusPosition;
    public byte bZoomState;
    public byte bReserved;
    public short wZoomRatio; // WORD in C is typically mapped to short in Java
    public NativeLong dwSequence; // Same as above for DWORD
    public NativeLong dwUTC; // Same as above for DWORD
    public int emPresetStatus; // For enums, you could use int in Java directly
    public int nPTZPanHD;
    public int nPTZTiltHD;
    public int nPTZZoomHD;
    public int[] reserved = new int[245]; // Arrays are direct mappings in JNA
}
