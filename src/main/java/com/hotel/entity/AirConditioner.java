package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 空调实体类
 */
@Data
@Accessors(chain = true)
@TableName("air_conditioners")
public class AirConditioner {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 空调编号
     */
    private Integer acNumber;
    
    /**
     * 当前服务的房间ID
     */
    private Long servingRoomId;
    
    /**
     * 是否开启
     */
    @TableField("on_status")
    private Boolean on;
    
    /**
     * 工作模式：COOLING(制冷), HEATING(制热)
     */
    private String mode;
    
    /**
     * 风速：LOW(低), MEDIUM(中), HIGH(高)
     */
    private String fanSpeed;
    
    /**
     * 目标温度
     */
    private Double targetTemp;
    
    /**
     * 当前温度
     */
    private Double currentTemp;
    
    /**
     * 请求时间
     */
    private LocalDateTime requestTime;
    
    /**
     * 服务开始时间
     */
    private LocalDateTime serviceStartTime;
    
    /**
     * 服务结束时间
     */
    private LocalDateTime serviceEndTime;
    
    /**
     * 服务时长(分钟)
     */
    private Long serviceDuration;
    
    /**
     * 当前费用
     */
    private Double cost;
    
    /**
     * 优先级(基于风速)
     */
    private Integer priority;
    
    /**
     * 服务时间(分钟)
     */
    private Long serviceTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 