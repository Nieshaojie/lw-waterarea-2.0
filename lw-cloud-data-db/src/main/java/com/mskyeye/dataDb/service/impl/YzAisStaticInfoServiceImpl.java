package com.mskyeye.dataDb.service.impl;

import com.mskyeye.dataDb.mapper.YzAisStaticInfoMapper;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.mskyeye.dataDb.service.IYzAisStaticInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AIS静态信息Service业务层处理
 *
 * @author ruoyi
 * @date 2023-05-25
 */
@Service
public class YzAisStaticInfoServiceImpl implements IYzAisStaticInfoService
{
    @Autowired
    private YzAisStaticInfoMapper yzAisStaticInfoMapper;

    /**
     * 查询AIS静态信息
     *
     * @param mmsi AIS静态信息主键
     * @return AIS静态信息
     */
    @Override
    public YzAisStaticInfo selectYzAisStaticInfoByMmsi(Long mmsi)
    {
        return yzAisStaticInfoMapper.selectYzAisStaticInfoByMmsi(mmsi);
    }

    /**
     * 查询AIS静态信息列表
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return AIS静态信息
     */
    @Override
    public List<YzAisStaticInfo> selectYzAisStaticInfoList(YzAisStaticInfo yzAisStaticInfo)
    {
        return yzAisStaticInfoMapper.selectYzAisStaticInfoList(yzAisStaticInfo);
    }

    /**
     * 新增AIS静态信息
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return 结果
     */
    @Override
    public int insertYzAisStaticInfo(YzAisStaticInfo yzAisStaticInfo)
    {
        return yzAisStaticInfoMapper.insertYzAisStaticInfo(yzAisStaticInfo);
    }

    /**
     * 修改AIS静态信息
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return 结果
     */
    @Override
    public int updateYzAisStaticInfo(YzAisStaticInfo yzAisStaticInfo)
    {
        return yzAisStaticInfoMapper.updateYzAisStaticInfo(yzAisStaticInfo);
    }

    /**
     * 批量删除AIS静态信息
     *
     * @param mmsis 需要删除的AIS静态信息主键
     * @return 结果
     */
    @Override
    public int deleteYzAisStaticInfoByMmsis(Long[] mmsis)
    {
        return yzAisStaticInfoMapper.deleteYzAisStaticInfoByMmsis(mmsis);
    }

    /**
     * 删除AIS静态信息信息
     *
     * @param mmsi AIS静态信息主键
     * @return 结果
     */
    @Override
    public int deleteYzAisStaticInfoByMmsi(Long mmsi)
    {
        return yzAisStaticInfoMapper.deleteYzAisStaticInfoByMmsi(mmsi);
    }
}

