package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.AirConditioner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 空调Mapper接口
 */
@Mapper
public interface AirConditionerMapper extends BaseMapper<AirConditioner> {
    
    /**
     * 根据空调编号查询
     */
    @Select("SELECT * FROM air_conditioners WHERE ac_number = #{acNumber}")
    AirConditioner findByAcNumber(Integer acNumber);
} 