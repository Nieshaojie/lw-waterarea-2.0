package com.mskyeye.shipData.mapper;


import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AIS静态信息Mapper接口
 *
 * @author ruoyi
 * @date 2023-05-25
 */
@Mapper
public interface YzAisStaticInfoMapper
{
    /**
     * 查询AIS静态信息
     *
     * @param mmsi AIS静态信息主键
     * @return AIS静态信息
     */
    public YzAisStaticInfo selectYzAisStaticInfoByMmsi(Long mmsi);

    /**
     * 查询AIS静态信息列表
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return AIS静态信息集合
     */
    public List<YzAisStaticInfo> selectYzAisStaticInfoList(YzAisStaticInfo yzAisStaticInfo);

    /**
     * 新增AIS静态信息
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return 结果
     */
    public int insertYzAisStaticInfo(YzAisStaticInfo yzAisStaticInfo);

    /**
     * 修改AIS静态信息
     *
     * @param yzAisStaticInfo AIS静态信息
     * @return 结果
     */
    public int updateYzAisStaticInfo(YzAisStaticInfo yzAisStaticInfo);

    /**
     * 删除AIS静态信息
     *
     * @param mmsi AIS静态信息主键
     * @return 结果
     */
    public int deleteYzAisStaticInfoByMmsi(Long mmsi);

    /**
     * 批量删除AIS静态信息
     *
     * @param mmsis 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteYzAisStaticInfoByMmsis(Long[] mmsis);
}

