package com.mskyeye.trace.service;


import com.mskyeye.trace.model.YzAlarmEvent;


/**
 * 告警事件Service接口
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
public interface IYzAlarmEventService 
{

    /**
     * 新增告警事件
     * 
     * @param yzAlarmEvent 告警事件
     * @return 结果
     */
    public int insertYzAlarmEvent(YzAlarmEvent yzAlarmEvent);

}
