package com.mskyeye.handler.mapper;

import com.mskyeye.handler.model.RadarAlarmCalInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YzRadarAlarmCalInfoMapper {

    public List<RadarAlarmCalInfo> selectRadarAlarmCalInfoList()throws Exception;

}
