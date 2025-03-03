package com.mskyeye.trace.model;

import com.mskyeye.trace.annotation.BeijingTimeFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 告警事件对象 yz_alarm_event
 *
 * @author ruoyi
 * @date 2023-07-06
 */
@Data
public class YzAlarmEvent {

    /**
     * 告警事件id
     */
    private Long id;

    /**
     * 相机id
     */
    private Long cameraId;

    /**
     * 相机名称
     */
    private String cameraName;

    /**
     * 机构id
     */
    private Long deptId;

    /**
     * AI点位名称
     */
    private String aiPointName;

    /**
     * 事件类型
     */
    private Long eventType;

    /**
     * 事件状态
     */
    private Long status = 1L;

    /**
     * 经度
     */
    private Double lon;

    /**
     * 纬度
     */
    private Double lat;

    /**
     * 告警时间
     */
    @BeijingTimeFormat
    private Date alarmTime;

    /**
     * 图片地址1
     */
    private String photoUrl1;

    /**
     * 图片地址2
     */
    private String photoUrl2;

    /**
     * 图片地址3
     */
    private String photoUrl3;

    /**
     * 图片地址4
     */
    private String photoUrl4;

    /**
     * 图片地址5
     */
    private String photoUrl5;

    /**
     * 图片地址列表，用于示意图显示
     */
    private List<String> photoUrlList;

    /**
     * 视频地址
     */
    private String videoUrl;

}

