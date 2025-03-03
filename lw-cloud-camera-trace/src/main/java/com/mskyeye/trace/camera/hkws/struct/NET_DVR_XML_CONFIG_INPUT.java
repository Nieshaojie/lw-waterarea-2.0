package com.mskyeye.trace.camera.hkws.struct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName:NET_DVR_XML_CONFIG_INPUT
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/20 16:31
 * @Version:1.0
 **/
public class NET_DVR_XML_CONFIG_INPUT extends Structure {

    public int dwSize;
    public Pointer lpRequestUrl;
    public int dwRequestUrlLen;
    public Pointer lpInBuffer;
    public int dwInBufferSize;
    public int dwRecvTimeOut;
    public byte byForceEncrpt;
    public byte byNumOfMultiPart;
    public byte[] byRes = new byte[30];

    public NET_DVR_XML_CONFIG_INPUT() {
        super();
    }

    public NET_DVR_XML_CONFIG_INPUT(Pointer pointer) {
        super(pointer);
        read();
    }
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "dwSize",
                "lpRequestUrl",
                "dwRequestUrlLen",
                "lpInBuffer",
                "dwInBufferSize",
                "dwRecvTimeOut",
                "byForceEncrpt",
                "byNumOfMultiPart",
                "byRes"
        );
    }
}
