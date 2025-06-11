package com.hotel.service;

/**
 * 空调调度服务
 * 实现风速优先+时间片轮转的调度策略
 */
public interface ACScheduleService {
    /**
     * 开启空调
     *
     * @param roomId      房间ID
     * @param currentTemp 当前温度
     * @return 开启结果
     */
    String startAC(Long roomId, Double currentTemp);

    /**
     * 修改温度
     *
     * @param roomId      房间ID
     * @param targetTemp  目标温度
     * @return 修改结果
     */
    String changeTemp(Long roomId, Double targetTemp);
} 