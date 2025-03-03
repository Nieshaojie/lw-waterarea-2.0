package com.mskyeye.trace.model;

/**
 * @ClassName:GplCmdSender
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/6/12 15:46
 * @Version:1.0
 **/
public class GplCmdSender {

//    private ExecutorService executor = Executors.newFixedThreadPool(10); // 创建一个包含10个线程的线程池

    public void sendCommand(YzCameraInfo cameraInfo, byte[] command) {
//        executor.submit(() -> {
        try {
            if (cameraInfo.getManu().equals("gpl") && cameraInfo.getIsAvAlarm() == 1) {
                cameraInfo.getGplCtrlTcpClient().sendInfo(command);
            }
        } catch (Exception e) {
            // 异常处理逻辑
        }
//        });
    }

//    @PreDestroy
//    public void shutdown() {
//        executor.shutdown();
//    }
}
