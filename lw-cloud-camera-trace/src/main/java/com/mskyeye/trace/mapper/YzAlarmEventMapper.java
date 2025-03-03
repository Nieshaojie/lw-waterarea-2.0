package com.mskyeye.trace.mapper;


import com.mskyeye.trace.model.YzAlarmEvent;
import org.apache.ibatis.annotations.Mapper;


/**
 * 告警事件Mapper接口
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
@Mapper
public interface YzAlarmEventMapper 
{

    /**
     * 新增告警事件
     * 
     * @param yzAlarmEvent 告警事件
     * @return 结果
     */
    public int insertYzAlarmEvent(YzAlarmEvent yzAlarmEvent);


}
