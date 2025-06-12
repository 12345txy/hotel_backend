package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.BillDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账单详单Mapper接口
 */
@Mapper
public interface BillDetailMapper extends BaseMapper<BillDetail> {
    
    /**
     * 根据客户ID查询账单详单
     */
    @Select("SELECT * FROM bill_details WHERE customer_id = #{customerId} ORDER BY start_time")
    List<BillDetail> findByCustomerId(Long customerId);
    
    /**
     * 根据房间号查询账单详单
     */
    @Select("SELECT * FROM bill_details WHERE room_number = #{roomNumber} ORDER BY start_time")
    List<BillDetail> findByRoomNumber(Integer roomNumber);
    
    /**
     * 根据账单ID查询详单
     */
    @Select("SELECT * FROM bill_details WHERE bill_id = #{billId} ORDER BY start_time DESC")
    List<BillDetail> findByBillId(Long billId);
} 