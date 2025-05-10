package com.mskyeye.trace.common;

import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;
import com.mskyeye.trace.model.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName:GlResources
 * @Description:全局资源
 * @Author:R.Gong
 * @Date:2023/8/7 18:50
 * @Version:1.0
 **/
public class GlResources {

    //key:相机ID Val:相机信息
    public static ConcurrentHashMap<Long, YzCameraInfo> GL_CameraInfoMap = new ConcurrentHashMap<>();

    //跟踪Map。key是相机ID，val是跟踪信息
    public static ConcurrentHashMap<Long, TraceProInfo> GL_TraceInfoMap = new ConcurrentHashMap<>();

    //AI巡航队列
    public static ConcurrentHashMap<Long, YzAiCruiseInfo> GL_CruiseMap = new ConcurrentHashMap<>();

    //当前正在巡航的AI点位Map,key是相机ID,val是点位信息
    public static ConcurrentHashMap<Long, YzAiPointInfo> GL_CurPointInfoMap = new ConcurrentHashMap<>();

    //雷光警戒预警目标缓存信息
    public static ConcurrentHashMap<Long, LwTrackPacket> GL_RCAlarmMap = new ConcurrentHashMap<>();

    //相机状态Map
    public static ConcurrentHashMap<Long, LwCameraStatusPacket> GL_CameraStatusMap = new ConcurrentHashMap<>();

    //用于执行雷光警戒抓拍动作的线程池
    public static ExecutorService executor;

    //用于获取相机状态的线程池
    public static ExecutorService executor1;

    public static final String CAMERA_INFO_CACHE = "yz2.0_info:yz_camera_info";

    public static final String CAMERA_STATE_BY_ISC = "yz2.0_info:yz_camera_state";

    public static DeviceMonitorInfo Gl_MonitorInfo = new DeviceMonitorInfo();

    public static final String CRUISE_STATE = "yz2.0_info:yz_cruise_state";

    public static final String MSG_TOPIC = "DEVICE_MONITOR_MESSAGE_QUEUE_KEY";

    public static final String DETECT_KEY = "FISHING_DETECT_MESSAGE_QUEUE_KEY";


    /**
     * 计算相机T值
     *
     * @param name
     * @param dis
     * @param bear
     * @return
     */
    public static Double calTVal(String name, Double dis, Double bear) {
        Double result = null;
        double p1, p2, p3, p4, p5, p6, p7, p8 = 0;
        double x = dis;
        switch (name) {
            case "松树园":
                if (bear >= 0 && bear <= 190) {
                    p1 = -8.9977e-12;
                    p2 = 1.7517e-07;
                    p3 = -0.0012234;
                    p4 = 3.0083;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 190 && bear <= 210) {
                    p1 = -4.2424e-12;
                    p2 = 1.2567e-07;
                    p3 = -0.0010935;
                    p4 = 2.9859;
                    x = x > 6000 ? 6000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 210 && bear <= 230) {
                    p1 = -6.6667e-12;
                    p2 = 1.5143e-07;
                    p3 = -0.0011569;
                    p4 = 3.0714;
                    x = x > 5000 ? 5000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 230 && bear <= 250) {
                    p1 = -2.2222e-11;
                    p2 = 3.1905e-07;
                    p3 = -0.0017178;
                    p4 = 3.6781;
                    x = x > 5000 ? 5000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 250 && bear <= 270) {
                    p1 = -4.7242e-12;
                    p2 = 1.2235e-07;
                    p3 = -0.0010307;
                    p4 = 3.0231;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 270 && bear <= 290) {
                    p1 = -4.3357e-12;
                    p2 = 1.1058e-07;
                    p3 = -0.00094344;
                    p4 = 2.895;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 290 && bear <= 310) {
                    p1 = -2.4848e-11;
                    p2 = 3.8727e-07;
                    p3 = -0.0021298;
                    p4 = 4.5779;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 310 && bear <= 360) {
                    p1 = -2.2751e-11;
                    p2 = 3.7371e-07;
                    p3 = -0.0021389;
                    p4 = 4.7303;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                }
                break;

            case "双廊镇":
                if (bear >= 0 && bear <= 205) {
                    p1 = -3.4667e-17; //-7.2941e-18;
                    p2 = 5.9818e-13;//1.5687e-13;
                    p3 = -4.0312e-09;//-1.3066e-09;
                    p4 = 13332e-05;//5.2728e-06;
                    p5 = -0.022164;//-0.010556;
                    p6 = 17.1986;//10.509;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3)
                            + p4 * Math.pow(x, 2) + p5 * x + p6;
                } else if (bear > 205 && bear <= 215) {
                    p1 = -1.3463e-17;
                    p2 = 2.8612e-13;
                    p3 = -2.338e-09;
                    p4 = 9.1746e-06;
                    p5 = -0.01757;
                    p6 = 15.2273;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3)
                            + p4 * Math.pow(x, 2) + p5 * x + p6;
                } else if (bear > 215 && bear <= 230) {
                    p1 = -2.0275e-17;
                    p2 = 4.2304e-13;
                    p3 = -3.3803e-09;
                    p4 = 1.2917e-05;
                    p5 = -0.023945;
                    p6 = 19.3433;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3)
                            + p4 * Math.pow(x, 2) + p5 * x + p6;
                } else if (bear > 230 && bear <= 250) {
                    p1 = -2.8915e-11;
                    p2 = 4.2822e-07;
                    p3 = -0.0022948;
                    p4 = 5.2725;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 250 && bear <= 270) {
                    p1 = -3.8746e-11;
                    p2 = 5.0385e-07;
                    p3 = -0.0024173;
                    p4 = 4.9186;
                    x = x > 6500 ? 6500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear > 270 && bear <= 360) {
                    p1 = -2.8267e-11;
                    p2 = 4.2716e-07;
                    p3 = -0.0023444;
                    p4 = 4.4797;
                    x = x > 6000 ? 6000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                }
                break;

            case "海东方":
                x = x > 6500 ? 6500 : x;
                if (bear >= 0 && bear <= 190) {
                    p1 = -2.7897e-18;
                    p2 = 5.7044e-14;
                    p3 = -4.4489e-10;
                    p4 = 1.7416e-06;
                    p5 = -0.0041197;
                    p6 = 5.7128;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 190 && bear <= 210) {
                    p1 = 3.5282e-18;
                    p2 = -5.2131e-14;
                    p3 = 2.0194e-10;
                    p4 = 2.9485e-07;
                    p5 = -0.0034605;
                    p6 = 6.2303;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 210 && bear <= 230) {
                    p1 = -3.241e-18;
                    p2 = 9.0434e-14;
                    p3 = -9.703e-10;
                    p4 = 5.0409e-06;
                    p5 = -0.012953;
                    p6 = 13.2459;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 230 && bear <= 250) {
                    p1 = 4.1026e-18;
                    p2 = -8.0839e-14;
                    p3 = 5.8098e-10;
                    p4 = -1.736e-06;
                    p5 = 0.0011745;
                    p6 = 1.9981;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 250 && bear <= 270) {
                    p1 = 1.8462e-18;
                    p2 = -2.8159e-14;
                    p3 = 1.1256e-10;
                    p4 = 2.1692e-07;
                    p5 = -0.0025428;
                    p6 = 4.3848;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 270 && bear <= 290) {
                    p1 = -3.3641e-18;
                    p2 = 8.1138e-14;
                    p3 = -7.533e-10;
                    p4 = 3.3906e-06;
                    p5 = -0.0077369;
                    p6 = 7.2956;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 290 && bear <= 310) {
                    p1 = 2.9046e-17;
                    p2 = -5.4907e-13;
                    p3 = 3.9588e-09;
                    p4 = -1.3616e-05;
                    p5 = 0.022083;
                    p6 = -12.8703;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                } else if (bear > 310 && bear <= 360) {
                    p1 = -2.2154e-18;
                    p2 = 6.2881e-14;
                    p3 = -6.5918e-10;
                    p4 = 3.2161e-06;
                    p5 = -0.0076909;
                    p6 = 7.984;
                    result = p1 * Math.pow(x, 5) + p2 * Math.pow(x, 4) + p3 * Math.pow(x, 3) + p4 * Math.pow(x, 2)
                            + p5 * x + p6;
                }
                break;
            case "藻水分离站(声光)":
                if (bear >= 0 && bear < 20) {
                    p1 = -2.2222e-11;
                    p2 = 3.2667e-07;
                    p3 = -0.0015511;
                    p4 = 2.87;
                    x = x > 3000 ? 3000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 20 && bear < 40) {
                    p1 = -5.9259e-11;
                    p2 = 5.8254e-07;
                    p3 = -0.0021139;
                    p4 = 3.1367;
                    x = x > 3000 ? 3000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 40 && bear < 60) {
                    p1 = -8.5185e-11;
                    p2 = 8.2937e-07;
                    p3 = -0.0029126;
                    p4 = 3.6533;
                    x = x > 3000 ? 3000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 60 && bear < 80) {
                    p1 = -2.6963e-10;
                    p2 = 1.8813e-06;
                    p3 = -0.0046834;
                    p4 = 4.4233;
                    x = x > 3000 ? 3000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 80 && bear < 100) {
                    p1 = -1.1441e-10;
                    p2 = 1.1789e-06;
                    p3 = -0.0040548;
                    p4 = 4.3896;
                    x = x > 4500 ? 4500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 100 && bear < 120) {
                    p1 = -5.0303e-11;
                    p2 = 6.1143e-07;
                    p3 = -0.0025027;
                    p4 = 3.0005;
                    x = x > 4500 ? 4500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 120 && bear < 140) {
                    p1 = -4.8889e-11;
                    p2 = 5.3905e-07;
                    p3 = -0.0020202;
                    p4 = 2.1957;
                    x = x > 4500 ? 4500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 140 && bear < 340) {
                    p1 = -4.8889e-11;
                    p2 = 5.3905e-07;
                    p3 = -0.0020202;
                    p4 = 2.1957;
                    x = x > 3500 ? 3500 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                } else if (bear >= 340 && bear <= 360) {
                    p1 = -2.2222e-11;
                    p2 = 3.2667e-07;
                    p3 = -0.0015511;
                    p4 = 2.87;
                    x = x > 3000 ? 3000 : x;
                    result = p1 * Math.pow(x, 3) + p2 * Math.pow(x, 2) + p3 * x + p4;
                }
                break;
            case "挖色小普陀(声光)":
                if (x >= 0 && x < 1000) {
                    result = 7.0;
                } else if (x >= 1000 && x < 1500) {
                    result = 5.6;
                } else if (x >= 1500 && x < 2000) {
                    result = 4.5;
                } else if (x >= 2000 && x < 2500) {
                    result = 3.67;
                } else if (x >= 2500 && x < 3000) {
                    result = 3.5;
                } else if (x >= 3000 && x < 3500) {
                    result = 3.44;
                } else if (x >= 3500 && x < 4000) {
                    result = 3.18;
                } else if (x >= 4000 && x < 4500) {
                    result = 3.12;
                } else if (x >= 4500 && x < 5000) {
                    result = 3.08;
                } else if (x >= 5000) {
                    result = 3.0;
                }
                break;
            case "龙凤村搬迁(声光)":
                if (x >= 0 && x < 1000) {
                    result = 2.79;
                } else if (x >= 1000 && x < 1500) {
                    result = 1.09;
                } else if (x >= 1500 && x < 2000) {
                    result = 0.6;
                } else if (x >= 2000 && x < 2500) {
                    result = 0.17;
                } else if (x >= 2500 && x < 3000) {
                    result = -0.07;
                } else if (x >= 3000 && x < 3500) {
                    result = -0.6;
                } else if (x >= 3500 && x < 4000) {
                    result = -0.7;
                } else if (x >= 4000 && x < 4500) {
                    result = -0.76;
                } else if (x >= 4500 && x < 5000) {
                    result = -0.8;
                } else if (x >= 5000) {
                    result = -0.82;
                }
                break;
            case "景观台(声光)":
                if (x >= 0 && x < 1000) {
                    result = 9.47;
                } else if (x >= 1000 && x < 1500) {
                    result = 4.63;
                } else if (x >= 1500 && x < 2000) {
                    result = 3.44;
                } else if (x >= 2000 && x < 2500) {
                    result = 2.57;
                } else if (x >= 2500 && x < 3000) {
                    result = 2.3;
                } else if (x >= 3000 && x < 3500) {
                    result = 2.0;
                } else if (x >= 3500 && x < 4000) {
                    result = 1.8;
                } else if (x >= 4000 && x < 4500) {
                    result = 1.62;
                } else if (x >= 4500 && x < 5000) {
                    result = 1.5;
                } else if (x >= 5000) {
                    result = 1.4;
                }
                break;
            case "大湾庄(声光)":
                if (x >= 0 && x < 1000) {
                    result = 3.34;
                } else if (x >= 1000 && x < 1500) {
                    result = 0.85;
                } else if (x >= 1500 && x < 2000) {
                    result = 0.26;
                } else if (x >= 2000 && x < 2500) {
                    result = -0.22;
                } else if (x >= 2500 && x < 3000) {
                    result = -0.4;
                } else if (x >= 3000 && x < 3500) {
                    result = -0.65;
                } else if (x >= 3500 && x < 4000) {
                    result = -0.78;
                } else if (x >= 4000 && x < 4500) {
                    result = -0.82;
                } else if (x >= 4500 && x < 5000) {
                    result = -0.87;
                } else if (x >= 5000) {
                    result = -0.94;
                }
                break;
        }
        if (result != null) {
            System.out.println("曲线拟合方法计算的T值为：" + result);
        }
        return result;
    }


    /**
     * 计算相机可见光Z值
     *
     * @param dis
     * @return
     */
    public static Double calZVal(Double dis) {
        //福州
        Double result = null;
        if (dis >= 0 && dis <= 500) {
            result = 20.0;
        } else if (dis > 500 && dis <= 1000) {
            result = 20.0;
        } else if (dis > 1000 && dis <= 1500) {
            result = 30.0;
        } else if (dis > 1500 && dis <= 2000) {
            result = 32.0;
        } else if (dis > 2000 && dis <= 2500) {
            result = 35.0;
        } else if (dis > 2500 && dis <= 3000) {
            result = 40.0;
        } else if (dis > 3000 && dis <= 3500) {
            result = 43.0;
        } else if (dis > 3500 && dis <= 4000) {
            result = 46.0;
        } else if (dis > 4000 && dis <= 4500) {
            result = 50.0;
        } else if (dis > 4500 && dis <= 5000) {
            result = 53.0;
        } else if (dis > 5000 && dis <= 5500) {
            result = 55.0;
        } else if (dis > 5500 && dis <= 6000) {
            result = 58.0;
        } else if (dis > 6000 && dis <= 6500) {
            result = 60.0;
        } else if (dis > 6500 && dis <= 7500) {
        result = 63.0;
        }else if (dis > 7500 && dis <= 8500) {
            result = 65.0;
        }else if (dis > 8500 && dis <= 9500) {
            result = 68.0;
        }else if (dis > 9500 && dis <= 10500) {
            result = 70.0;
        }else if (dis > 10500 && dis <= 12500) {
            result = 72.0;
        }else if (dis > 12500 && dis <= 14500) {
            result = 75.0;
        }
        else if (dis > 14500) {
            result = 80.0;
        }
        return result;
        //云南
//        Double result = null;
//        if (dis >= 0 && dis <= 500) {
//            result = 32.0;
//        } else if (dis > 500 && dis <= 1000) {
//            result = 32.0;
//        } else if (dis > 1000 && dis <= 1500) {
//            result = 40.0;
//        } else if (dis > 1500 && dis <= 2000) {
//            result = 47.0;
//        } else if (dis > 2000 && dis <= 2500) {
//            result = 50.0;
//        } else if (dis > 2500 && dis <= 3000) {
//            result = 58.0;
//        } else if (dis > 3000 && dis <= 3500) {
//            result = 59.0;
//        } else if (dis > 3500 && dis <= 4000) {
//            result = 59.0;
//        } else if (dis > 4000 && dis <= 4500) {
//            result = 60.0;
//        } else if (dis > 4500 && dis <= 5000) {
//            result = 60.0;
//        } else if (dis > 5000 && dis <= 5500) {
//            result = 61.0;
//        } else if (dis > 5500 && dis <= 6000) {
//            result = 61.5;
//        } else if (dis > 6000 && dis <= 6500) {
//            result = 62.0;
//        } else if (dis > 6500) {
//            result = 62.5;
//        }
//        return result;
    }

    /**
     * 计算相机热像Z值
     *
     * @param dis
     * @return
     */
    public static Double calRZVal(Double dis) {
        Double result = null;
        if (dis >= 0 && dis <= 500) {
            result = 5.0;
        } else if (dis > 500 && dis <= 1000) {
            result = 8.0;
        } else if (dis > 1000 && dis <= 1500) {
            result = 10.0;
        } else if (dis > 1500 && dis <= 2000) {
            result = 12.0;
        } else if (dis > 2000 && dis <= 2500) {
            result = 14.0;
        } else if (dis > 2500 && dis <= 3000) {
            result = 16.0;
        } else if (dis > 3000 && dis <= 3500) {
            result = 18.0;
        } else if (dis > 3500 && dis <= 4000) {
            result = 20.0;
        } else if (dis > 4000 && dis <= 4500) {
            result = 23.0;
        } else if (dis > 4500 && dis <= 5000) {
            result = 25.0;
        } else if (dis > 5000 && dis <= 5500) {
            result = 27.0;
        } else if (dis > 5500 && dis <= 6000) {
            result = 29.0;
        } else if (dis > 6000 && dis <= 6500) {
            result = 31.0;
        } else if (dis > 6500 && dis <= 7500) {
            result = 35.0;
        }else if (dis > 7500 && dis <= 8500) {
            result = 36.0;
        }else if (dis > 8500 && dis <= 9500) {
            result = 37.0;
        }else if (dis > 9500 && dis <= 10500) {
            result = 40.0;
        }else if (dis > 10500 && dis <= 12500) {
            result = 43.0;
        }else if (dis > 12500 && dis <= 14500) {
            result = 45.0;
        }else if (dis > 14500 && dis <= 16500) {
            result = 47.0;
        }else if (dis > 16500 && dis <= 18500) {
            result = 50.0;
        }
        else if (dis > 18500) {
            result = 55.0;
        }
        return result;
    }
}