package com.mskyeye.trace.service;


import com.mskyeye.trace.model.YzAiCruiseInfo;

import java.util.List;
import java.util.Map;


/**
 * AI巡航信息Service接口
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
public interface IYzAiCruiseInfoService 
{
    /**
     * 查询AI巡航信息
     * 
     * @param id AI巡航信息主键
     * @return AI巡航信息
     */
    public YzAiCruiseInfo selectYzAiCruiseInfoById(Long id);

    /**
     * 查询AI巡航信息列表
     * 
     * @param yzAiCruiseInfo AI巡航信息
     * @return AI巡航信息集合
     */
    public List<YzAiCruiseInfo> selectYzAiCruiseInfoList(YzAiCruiseInfo yzAiCruiseInfo);


    /**
     * 查询AI巡航状态
     * @return
     */
    public Map<Long, Map<Object,Object>> status();
}
