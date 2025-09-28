package com.mskyeye.lwradarstationdata.protocol.radar.custom;

import lombok.Data;

import java.io.Serializable;

/**
 * 协议 UPLOAD_TRACK_V3 封装对象
 *
 * 对应雷达通信协议中的【3.x 上报目标信息指令 (UPLOAD_TRACK_V3, 指令码 0x00030002)】。
 * 每次扫描周期，雷达都会上传一个航迹数据帧，包含多个目标的检测信息。
 */
@Data
public class FlyTrackTcpPacket implements Serializable {

    private byte bAdd = 1;                       // 1:更新, 0:清除

    private int stationId;                   /*探测站ID*/

    // ================= 帧公共字段 =================
    private long utcTimestamp;                    // 雷达启动 UTC 时间戳 (µs)
    private long frameId;                         // 帧号
    private long uploadTimestamp;                 // 上传时间戳 (ms)
    private double startAngle;                    // 起始角度 (deg)
    private double endAngle;                      // 结束角度 (deg)
    private boolean northReference;               // 北向参考
    private int scanDirection;                    // 扫描方向
    private int targetCount;                      // 当前帧目标数量

    // ================= 原始局部坐标 =================
    private double x;                             // X坐标 (m)
    private double y;                             // Y坐标 (m)
    private double z;                             // Z坐标 (m)

    // ================= 目标速度 =================
    private double vx;                            // X速度 (m/s)
    private double vy;                            // Y速度 (m/s)
    private double vz;                            // Z速度 (m/s)
    private double speed3D;                        // 三维速度 (m/s)
    private double speedHorizontal;               // 水平速度 (m/s)

    // ================= 目标属性 =================
    private long targetId;                        // 目标ID
    private double snr;                           // 信噪比
    private double rcs;                           // 低精度RCS
    private int type;                             // 目标类型
    private boolean selected;                     // 选中标志

    // ================= 预留字段 =================
    private short reserved1;                      // INT16 预留
    private int reserved2;                        // UINT16 预留
    private int reserved3;                        // UINT16 预留
    private byte reserved4;                       // INT8 预留
    private long reserved5;                       // INT64 预留
    private int reserved6;                        // INT32 预留
    private int reserved7;                        // INT32 预留
    private int reserved8;                        // INT32 预留，可选
    private double rcsHighPrecision;             // 高精度RCS, 分辨率 1e-6 m²

    // ================= 经纬高坐标 =================
    private double lat;
    private double lon;
    private double alt;
}
