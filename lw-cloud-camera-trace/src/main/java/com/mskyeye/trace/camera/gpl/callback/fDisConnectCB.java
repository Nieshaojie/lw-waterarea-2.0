package com.mskyeye.trace.camera.gpl.callback;

import com.sun.jna.NativeLong;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName:fDisConnectCB
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/9 16:12
 * @Version:1.0
 **/
public class fDisConnectCB implements fDisConnect {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Logger logger = Logger.getLogger(fDisConnectCB.class.getName());

    @Override
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser) {
        executor.submit(() -> {
            try {
                if (pchDVRIP != null) {
                    logger.info(String.format("\u274c \u65ad\u7ebf\u56de\u8c03: \u8bbe\u5907 [%s] \u7aef\u53e3 [%d]", pchDVRIP, nDVRPort));
                } else {
                    logger.warning("\u274c \u65ad\u7ebf\u56de\u8c03: IP \u5730\u5740\u4e3a null");
                }
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "\u65ad\u7ebf\u56de\u8c03\u6267\u884c\u5f02\u5e38", t);
            }
        });
    }
}

