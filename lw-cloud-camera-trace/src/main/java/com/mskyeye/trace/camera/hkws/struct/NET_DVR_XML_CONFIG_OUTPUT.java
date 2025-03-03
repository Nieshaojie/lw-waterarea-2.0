package com.mskyeye.trace.camera.hkws.struct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName:NET_DVR_XML_CONFIG_OUTPUT
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/20 17:14
 * @Version:1.0
 **/
public class NET_DVR_XML_CONFIG_OUTPUT extends Structure {

    public int dwSize;
    public Pointer lpOutBuffer;
    public int dwOutBufferSize;
    public int dwReturnedXMLSize;
    public Pointer lpStatusBuffer;
    public int dwStatusSize;
    public byte[] byRes = new byte[32];

    public NET_DVR_XML_CONFIG_OUTPUT() {
        super();
    }

    public NET_DVR_XML_CONFIG_OUTPUT(Pointer pointer) {
        super(pointer);
        read();
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("dwSize", "lpOutBuffer", "dwOutBufferSize", "dwReturnedXMLSize",
                "lpStatusBuffer", "dwStatusSize", "byRes");
    }

}
