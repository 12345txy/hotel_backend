package com.hotel.entity;

import com.hotel.enums.FanSpeed;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * 房间实体类
 */
@Data
public class Room {

    private final Long id;

//    /**
//     * 房间状态：AVAILABLE(可用), OCCUPIED(已入住), MAINTENANCE(维护中)
//     */
//    private String status;

    /**
     * 默认温度
     */
    private final Double defaultTemp;

    /**
     * 当前温度
     */
    private Double currentTemp;

    /**
     * 日租
     */
    private final Double dailyRate;


//    /**
//     * 入住客户ID
//     */
//    private Long customerId;
//
//    /**
//     * 入住时间
//     */
//    private LocalDateTime checkInTime;
//
//    /**
//     * 服务开始时间
//     */
//    private LocalDateTime serviceStartTime;
//
//    /**
//     * 等待开始时间
//     */
//    private LocalDateTime waitingStartTime;
//
//    /**
//     * 服务时长(秒)
//     */
//    private Long serviceDuration;
//
//    /**
//     * 等待时长(秒)
//     */
//    private Long waitingDuration;
//
//    /**
//     * 房间初始温度
//     */
//    private Double initialTemp;
//
//    /**
//     * 是否正在回温
//     */
//    private Boolean isWarmingBack;
//
//    /**
//     * 回温开始时间
//     */
//    private LocalDateTime warmingStartTime;
//
//    /**
//     * 暂停服务时的参数（用于回温后恢复）
//     */
//    private String pausedMode;
//
//    private String pausedFanSpeed;
//
//    private Double pausedTargetTemp;
//
//    /**
//     * 分配的空调编号
//     */
//    private Integer assignedAcNumber;
//
//    /**
//     * 队列状态：NONE(不在队列), SERVICE(服务队列), WAITING(等待队列)
//     */
//    private String queueStatus;
//
//    /**
//     * 在队列中的位置（1为队头）
//     */
//    private Integer queuePosition;
//
//    /**
//     * 队列优先级分数
//     */
//    private Double queuePriorityScore;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;

    /**
     * 构造函数
     * @param roomConfig 房间配置对象
     */
    public Room(RoomConfig roomConfig){
        this.id = roomConfig.getId();
        this.defaultTemp = roomConfig.getDefaultTemp();
        this.dailyRate = roomConfig.getDailyRate();
        this.currentTemp = this.defaultTemp;
    }

} 