package com.mskyeye.dataDb.service;

import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;

import java.util.List;

/**
 * AIS静态信息Service接口
 *
 * @author ruoyi
 * @date 2023-05-25
 */
public interface IYzAisStaticInfoService
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
     * 批量删除AIS静态信息
     *
     * @param mmsis 需要删除的AIS静态信息主键集合
     * @return 结果
     */
    public int deleteYzAisStaticInfoByMmsis(Long[] mmsis);

    /**
     * 删除AIS静态信息信息
     *
     * @param mmsi AIS静态信息主键
     * @return 结果
     */
    public int deleteYzAisStaticInfoByMmsi(Long mmsi);
}

