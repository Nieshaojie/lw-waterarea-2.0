package com.mskyeye.trace.mapper;

import com.mskyeye.trace.model.YzAiPointInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * AI点位信息Mapper接口
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@Mapper
public interface YzAiPointInfoMapper 
{
    /**
     * 查询AI点位信息
     * 
     * @param id AI点位信息主键
     * @return AI点位信息
     */
    public YzAiPointInfo selectYzAiPointInfoById(Long id);

    /**
     * 查询AI点位信息列表
     * 
     * @param yzAiPointInfo AI点位信息
     * @return AI点位信息集合
     */
    public List<YzAiPointInfo> selectYzAiPointInfoList(YzAiPointInfo yzAiPointInfo);

}
