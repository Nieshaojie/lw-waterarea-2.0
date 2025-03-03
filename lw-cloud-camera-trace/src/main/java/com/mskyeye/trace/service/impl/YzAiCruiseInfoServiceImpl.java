package com.mskyeye.trace.service.impl;


import com.mskyeye.trace.mapper.YzAiCruiseInfoMapper;
import com.mskyeye.trace.mapper.YzAiPointInfoMapper;
import com.mskyeye.trace.model.YzAiCruiseInfo;
import com.mskyeye.trace.model.YzAiPointInfo;
import com.mskyeye.trace.service.IYzAiCruiseInfoService;
import com.mskyeye.trace.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mskyeye.trace.common.GlResources.CRUISE_STATE;


/**
 * AI巡航信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@Service
public class YzAiCruiseInfoServiceImpl implements IYzAiCruiseInfoService 
{
    @Autowired
    private YzAiCruiseInfoMapper yzAiCruiseInfoMapper;

    @Autowired
    private YzAiPointInfoMapper yzAiPointInfoMapper;

    @Autowired
    private RedisCache redisCache;


    /**
     * 查询AI巡航信息
     * 
     * @param id AI巡航信息主键
     * @return AI巡航信息
     */
    @Override
    public YzAiCruiseInfo selectYzAiCruiseInfoById(Long id)
    {
        YzAiCruiseInfo yzAiCruiseInfo = yzAiCruiseInfoMapper.selectYzAiCruiseInfoById(id);
        String[] pointsIds = yzAiCruiseInfo.getPoints().split(",");
        List<YzAiPointInfo> tmpList = new ArrayList<>();
        for(String strId : pointsIds){
            YzAiPointInfo yzAiPointInfo = yzAiPointInfoMapper.selectYzAiPointInfoById(Long.valueOf(strId));
            if(yzAiPointInfo != null){
                tmpList.add(yzAiPointInfo);
            }
        }
        yzAiCruiseInfo.setPointsInfoList(tmpList);
        return yzAiCruiseInfo;
    }

    /**
     * 查询AI巡航信息列表
     * 
     * @param yzAiCruiseInfo AI巡航信息
     * @return AI巡航信息
     */
    @Override
    public List<YzAiCruiseInfo> selectYzAiCruiseInfoList(YzAiCruiseInfo yzAiCruiseInfo)
    {
        List<YzAiCruiseInfo> list = yzAiCruiseInfoMapper.selectYzAiCruiseInfoList(yzAiCruiseInfo);
        List<YzAiCruiseInfo> resultList = list.stream().map(obj->{
            List<YzAiPointInfo> tmpList = new ArrayList<>();
            String[] pointsIds = obj.getPoints().split(",");
            for(String strId : pointsIds){
                YzAiPointInfo yzAiPointInfo = yzAiPointInfoMapper.selectYzAiPointInfoById(Long.valueOf(strId));
                if(yzAiPointInfo != null){
                    tmpList.add(yzAiPointInfo);
                }
            }
            obj.setPointsInfoList(tmpList);
            return obj;
        }).collect(Collectors.toList());
        return resultList;
    }

    /**
     * 查询AI巡航状态
     * @return
     */
    @Override
    public Map<Long, Map<Object,Object>> status() {
        return redisCache.getCacheObject(CRUISE_STATE);
    }
}
