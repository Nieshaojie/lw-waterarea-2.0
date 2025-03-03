package com.mskyeye.ws.common;

import com.mskyeye.ws.model.DeviceInDept;
import org.yeauty.pojo.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @ClassName:GlobalResources
 * @Description:全局资源类
 * 包括雷达的航迹队列
 * @Author:R.Gong
 * @Date:2022/11/14 15:57
 * @Version:1.0
 **/
public class GlobalResources {

    //缓存航迹队列最大数
    public static final Integer TRACK_QUEUE_CAP = 200;

    //实时航迹队列最大数
    public static final Integer TRACK_QUEUE_CUR = 20;

    //缓存航迹队列
    public static ConcurrentLinkedDeque<String> capTrackQueue = new ConcurrentLinkedDeque<>();

    //实时航迹队列
    public static ConcurrentLinkedDeque<String> curTrackQueue = new ConcurrentLinkedDeque<>();

    //用户Session
    public static ConcurrentHashMap<Session, DeviceInDept> sessionKV = new ConcurrentHashMap<>();

    //设备信息Map,key是机构ID
    public static ConcurrentHashMap<Long, DeviceInDept> deviceInfoMap = new ConcurrentHashMap<>();
}
