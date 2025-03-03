package com.mskyeye.trace.service.impl;


import com.mskyeye.trace.mapper.YzAlarmEventMapper;
import com.mskyeye.trace.model.YzAlarmEvent;
import com.mskyeye.trace.service.IYzAlarmEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 告警事件Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-07-06
 */
@Service
public class YzAlarmEventServiceImpl implements IYzAlarmEventService
{
    @Autowired
    private YzAlarmEventMapper yzAlarmEventMapper;


    /**
     * 新增告警事件
     * 
     * @param yzAlarmEvent 告警事件
     * @return 结果
     */
    @Override
    public int insertYzAlarmEvent(YzAlarmEvent yzAlarmEvent)
    {
        return yzAlarmEventMapper.insertYzAlarmEvent(yzAlarmEvent);
    }

}
