package com.mskyeye.trace.camera.gpl.callback;


import com.sun.jna.NativeLong;

/**
 * @ClassName:HaveReConnect
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/9 16:16
 * @Version:1.0
 **/
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HaveReConnect implements fHaveReConnect {

    // 单线程池，防止线程竞争
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GPL-Reconnect-Callback");
        t.setDaemon(true); // 守护线程
        return t;
    });

    @Override
    public void invoke(NativeLong m_hLoginHandle, String pchDVRIP, int nDVRPort, NativeLong dwUser) {
        executor.submit(() -> {
            try {
                String ip = (pchDVRIP != null) ? pchDVRIP : "未知";
                System.out.printf("🔁 重连回调：设备 [%s] 端口 [%d]%n", ip, nDVRPort);

                // TODO: 这里可以加入自动补偿逻辑，如状态上报、任务恢复、日志上报等

            } catch (Exception e) {
                System.err.println("❌ ReConnect 回调异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
