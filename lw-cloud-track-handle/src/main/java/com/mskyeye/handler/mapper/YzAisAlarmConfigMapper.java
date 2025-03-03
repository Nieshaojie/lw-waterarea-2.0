package com.mskyeye.handler.mapper;

import com.mskyeye.handler.model.AisAlarmCalInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YzAisAlarmConfigMapper {

    public List<AisAlarmCalInfo> selectAisAlarmCalInfoList()throws Exception;
}
