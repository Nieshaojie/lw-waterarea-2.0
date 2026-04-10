package com.mskyeye.ws.serialPort;

public class RadarFrameUtil {

    /**
     * 构建雷达引导帧（32字节）
     * @param lat       纬度   double
     * @param lng       经度   double
     * @param alt       高度   单位 m
     * @param heading   航向   单位 °
     * @param speed     速度   单位 m/s
     * @return 完整32字节帧
     */
    public static byte[] buildRadarFrame(double lat, double lng, double alt,
                                         double heading, double speed) {
        byte[] frame = new byte[32];
        int index = 0;

        // 0 同步字1
        frame[index++] = (byte) 0xEE;
        // 1 同步字2
        frame[index++] = (byte) 0x18;

        // 2~5 纬度 S32 1e-7°
        int latVal = (int) (lat * 10000000);
        frame[index++] = (byte) (latVal >> 24);
        frame[index++] = (byte) (latVal >> 16);
        frame[index++] = (byte) (latVal >> 8);
        frame[index++] = (byte) latVal;

        // 6~9 经度 S32 1e-7°
        int lngVal = (int) (lng * 10000000);
        frame[index++] = (byte) (lngVal >> 24);
        frame[index++] = (byte) (lngVal >> 16);
        frame[index++] = (byte) (lngVal >> 8);
        frame[index++] = (byte) lngVal;

        // 10~11 高度 S16 0.1m
        short altVal = (short) (alt * 10);
        frame[index++] = (byte) (altVal >> 8);
        frame[index++] = (byte) altVal;

        // 12~13 航向 S16 0.1°
        short headVal = (short) (heading * 10);
        frame[index++] = (byte) (headVal >> 8);
        frame[index++] = (byte) headVal;

        // 14~15 速度 S16 0.1m/s
        short speedVal = (short) (speed * 10);
        frame[index++] = (byte) (speedVal >> 8);
        frame[index++] = (byte) speedVal;

        // 16 时 17 分 18 秒（取系统时间）
        java.time.LocalTime now = java.time.LocalTime.now();
        frame[index++] = (byte) now.getHour();
        frame[index++] = (byte) now.getMinute();
        frame[index++] = (byte) now.getSecond();

        // 19~20 百分秒 U16 10ms
        int millis = now.getNano() / 10_000_000; // 转成百分秒
        frame[index++] = (byte) (millis >> 8);
        frame[index++] = (byte) millis;

        // 21~30 预留 10字节 填0
        for (int i = 0; i < 10; i++) {
            frame[index++] = 0;
        }

        // 31 校验和（0~30 累加取低8位）
        int sum = 0;
        for (int i = 0; i < 31; i++) {
            sum += (frame[i] & 0xFF);
        }
        frame[31] = (byte) (sum & 0xFF);

        return frame;
    }
}