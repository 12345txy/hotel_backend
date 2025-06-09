package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单实体类
 */
@Data
@TableName("bills")
public class Bill {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 账单号
     */
    @TableField("bill_number")
    private String billNumber;
    
    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;
    
    /**
     * 客户ID
     */
    @TableField("customer_id")
    private Long customerId;
    
    /**
     * 客户姓名
     */
    @TableField("customer_name")
    private String customerName;
    
    /**
     * 入住时间
     */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    /**
     * 退房时间
     */
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;
    
    /**
     * 住宿天数
     */
    @TableField("stay_days")
    private Integer stayDays;
    
    /**
     * 房费(元)
     */
    @TableField("room_fee")
    private Double roomFee;
    
    /**
     * 空调总费用(元)
     */
    @TableField("ac_total_fee")
    private Double acTotalFee;
    
    /**
     * 账单总金额(元)
     */
    @TableField("total_amount")
    private Double totalAmount;
    
    /**
     * 账单状态：UNPAID-未支付, PAID-已支付, CANCELLED-已取消
     */
    @TableField("status")
    private String status;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 账单详单列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<BillDetail> billDetails;
} 