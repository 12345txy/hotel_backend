package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 房间实体类
 */
@Data
@Accessors(chain = true)
@TableName("rooms")
public class Room {
    
    @TableId(type = IdType.INPUT)
    private Long id;
    
    /**
     * 房间状态：AVAILABLE(可用), OCCUPIED(已入住), MAINTENANCE(维护中)
     */
    private String status;
    
    /**
     * 当前温度
     */
    private Double currentTemp;
    
    /**
     * 目标温度
     */
    private Double targetTemp;
    
    /**
     * 空调是否开启
     */
    private Boolean acOn;
    
    /**
     * 空调模式：COOLING(制冷), HEATING(制热)
     */
    private String acMode;
    
    /**
     * 风速：LOW(低), MEDIUM(中), HIGH(高)
     */
    private String fanSpeed;
    
    /**
     * 当前服务的空调ID
     */
    private Long currentAcId;
    
    /**
     * 入住客户ID
     */
    private Long customerId;
    
    /**
     * 入住时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 空调请求时间
     */
    private LocalDateTime acRequestTime;
    
    /**
     * 服务开始时间
     */
    private LocalDateTime serviceStartTime;
    
    /**
     * 等待开始时间
     */
    private LocalDateTime waitingStartTime;
    
    /**
     * 服务时长(秒)
     */
    private Long serviceDuration;
    
    /**
     * 等待时长(秒)
     */
    private Long waitingDuration;
    
    /**
     * 房间初始温度
     */
    private Double initialTemp;
    
    /**
     * 是否正在回温
     */
    private Boolean isWarmingBack;
    
    /**
     * 回温开始时间
     */
    private LocalDateTime warmingStartTime;
    
    /**
     * 暂停服务时的参数（用于回温后恢复）
     */
    private String pausedMode;
    
    private String pausedFanSpeed;
    
    private Double pausedTargetTemp;
    
    /**
     * 分配的空调编号
     */
    private Integer assignedAcNumber;
    
    /**
     * 队列状态：NONE(不在队列), SERVICE(服务队列), WAITING(等待队列)
     */
    private String queueStatus;
    
    /**
     * 在队列中的位置（1为队头）
     */
    private Integer queuePosition;
    
    /**
     * 队列优先级分数
     */
    private Double queuePriorityScore;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 