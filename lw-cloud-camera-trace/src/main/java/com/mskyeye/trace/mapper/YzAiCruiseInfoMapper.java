package com.mskyeye.trace.mapper;

import com.mskyeye.trace.model.YzAiCruiseInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * AI巡航信息Mapper接口
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@Mapper
public interface YzAiCruiseInfoMapper 
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

}
