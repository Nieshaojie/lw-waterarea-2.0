package com.mskyeye.handler.mapper;


import com.mskyeye.handler.model.YzCarInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 执法车辆信息Mapper接口
 * 
 * @author ruoyi
 * @date 2023-10-22
 */
@Mapper
public interface YzCarInfoMapper 
{
    /**
     * 查询执法车辆信息
     * 
     * @param id 执法车辆信息主键
     * @return 执法车辆信息
     */
    public YzCarInfo selectYzCarInfoById(Long id);

    /**
     * 查询执法车辆信息列表
     * 
     * @param yzCarInfo 执法车辆信息
     * @return 执法车辆信息集合
     */
    public List<YzCarInfo> selectYzCarInfoList(YzCarInfo yzCarInfo);

    /**
     * 新增执法车辆信息
     * 
     * @param yzCarInfo 执法车辆信息
     * @return 结果
     */
    public int insertYzCarInfo(YzCarInfo yzCarInfo);

    /**
     * 修改执法车辆信息
     * 
     * @param yzCarInfo 执法车辆信息
     * @return 结果
     */
    public int updateYzCarInfo(YzCarInfo yzCarInfo);

    /**
     * 删除执法车辆信息
     * 
     * @param id 执法车辆信息主键
     * @return 结果
     */
    public int deleteYzCarInfoById(Long id);

    /**
     * 批量删除执法车辆信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteYzCarInfoByIds(Long[] ids);
}
