package com.hotel.service;

import com.hotel.entity.Room;
import com.hotel.enums.AcMode;
import com.hotel.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 温度管理服务
 * 处理温度变化、回温逻辑和自动重启机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureService {
    
    private final RoomMapper roomMapper;
    private final AcScheduleService scheduleService;
    
    // 温度变化速率（每分钟）
    private static final double COOLING_RATE_HIGH = 1.0;    // 高风速制冷：1度/分钟
    private static final double COOLING_RATE_MEDIUM = 0.5;  // 中风速制冷：1度/2分钟
    private static final double COOLING_RATE_LOW = 0.33;    // 低风速制冷：1度/3分钟
    
    private static final double HEATING_RATE_HIGH = 1.0;    // 高风速制热：1度/分钟
    private static final double HEATING_RATE_MEDIUM = 0.5;  // 中风速制热：1度/2分钟
    private static final double HEATING_RATE_LOW = 0.33;    // 低风速制热：1度/3分钟
    
    private static final double WARMING_BACK_RATE = 0.5;    // 回温速率：0.5度/分钟
    private static final double WARMING_BACK_THRESHOLD = 1.0; // 回温阈值：1度
    
    /**
     * 更新房间温度（基于服务时长）
     */
    @Transactional
    public void updateRoomTemperature(Room room) {
        if (!Boolean.TRUE.equals(room.getAcOn()) || room.getCurrentAcId() == null) {
            // 没有空调服务时的自然回温
            handleNaturalWarming(room);
            return;
        }
        
        if (Boolean.TRUE.equals(room.getIsWarmingBack())) {
            // 正在回温中
            handleWarmingBack(room);
            return;
        }
        
        // 计算服务时长（分钟）
        long serviceMinutes = calculateServiceMinutes(room);
        if (serviceMinutes <= 0) return;
        
        // 根据空调模式和风速计算温度变化
        double tempChange = calculateTemperatureChange(room.getAcMode(), room.getFanSpeed(), serviceMinutes);
        double newTemp = room.getCurrentTemp();
        
        if (AcMode.COOLING.getCode().equals(room.getAcMode())) {
            newTemp = Math.max(newTemp - tempChange, room.getTargetTemp());
        } else if (AcMode.HEATING.getCode().equals(room.getAcMode())) {
            newTemp = Math.min(newTemp + tempChange, room.getTargetTemp());
        }
        
        room.setCurrentTemp(newTemp);
        roomMapper.updateById(room);
        
        // 检查是否达到目标温度
        if (Math.abs(newTemp - room.getTargetTemp()) < 0.1) {
            handleTargetTemperatureReached(room);
        }
    }
    
    /**
     * 处理达到目标温度的情况
     */
    @Transactional
    public void handleTargetTemperatureReached(Room room) {
                    log.info("房间{}达到目标温度{}，暂停空调服务", room.getId(), room.getTargetTemp());
        
        // 保存当前参数用于恢复
        room.setPausedMode(room.getAcMode())
            .setPausedFanSpeed(room.getFanSpeed())
            .setPausedTargetTemp(room.getTargetTemp())
            .setIsWarmingBack(true)
            .setWarmingStartTime(LocalDateTime.now());
        
        // 释放空调资源，生成详单
                    scheduleService.removeAcRequest(room.getId().intValue(), "达到目标温度");
        
        // 更新房间状态
        room.setAcOn(true) // 保持开启状态，但处于回温模式
            .setCurrentAcId(null)
            .setServiceStartTime(null)
            .setServiceDuration(0L);
        
        roomMapper.updateById(room);
    }
    
    /**
     * 处理回温逻辑
     */
    @Transactional
    public void handleWarmingBack(Room room) {
        LocalDateTime warmingStart = room.getWarmingStartTime();
        if (warmingStart == null) return;
        
        // 计算回温时间（分钟） - 按10秒实际时间=1分钟系统时间计算
        long warmingMinutes = Duration.between(warmingStart, LocalDateTime.now()).toSeconds() / 10;
        if (warmingMinutes <= 0) return;
        
        // 计算回温后的温度
        double tempChange = warmingMinutes * WARMING_BACK_RATE;
        double newTemp = room.getCurrentTemp();
        
        // 根据模式确定回温方向
        if (AcMode.COOLING.getCode().equals(room.getPausedMode())) {
            // 制冷模式回温：温度上升
            newTemp = Math.min(newTemp + tempChange, room.getInitialTemp());
        } else if (AcMode.HEATING.getCode().equals(room.getPausedMode())) {
            // 制热模式回温：温度下降
            newTemp = Math.max(newTemp - tempChange, room.getInitialTemp());
        }
        
        room.setCurrentTemp(newTemp);
        roomMapper.updateById(room);
        
        // 检查是否需要重新启动空调
        double tempDiff = Math.abs(newTemp - room.getPausedTargetTemp());
        if (tempDiff >= WARMING_BACK_THRESHOLD) {
            restartAcAfterWarming(room);
        }
    }
    
    /**
     * 回温后重新启动空调
     */
    @Transactional
    public void restartAcAfterWarming(Room room) {
                    log.info("房间{}回温1度，重新请求空调服务", room.getId());
        
        // 恢复之前的参数
        room.setAcMode(room.getPausedMode())
            .setFanSpeed(room.getPausedFanSpeed())
            .setTargetTemp(room.getPausedTargetTemp())
            .setIsWarmingBack(false)
            .setWarmingStartTime(null)
            .setAcRequestTime(LocalDateTime.now())
            .setPausedMode(null)
            .setPausedFanSpeed(null)
            .setPausedTargetTemp(null);
        
        roomMapper.updateById(room);
        
        // 重新添加到调度队列
        scheduleService.addAcRequest(room);
    }
    
    /**
     * 处理自然回温（关机状态）
     */
    @Transactional
    public void handleNaturalWarming(Room room) {
        if (room.getAcRequestTime() == null) return;
        
        // 计算关机时间 - 按10秒实际时间=1分钟系统时间计算
        long minutesSinceOff = Duration.between(room.getAcRequestTime(), LocalDateTime.now()).toSeconds() / 10;
        if (minutesSinceOff <= 0) return;
        
        double tempChange = minutesSinceOff * WARMING_BACK_RATE;
        double newTemp = room.getCurrentTemp();
        
        // 向初始温度回温
        if (room.getCurrentTemp() < room.getInitialTemp()) {
            newTemp = Math.min(newTemp + tempChange, room.getInitialTemp());
        } else if (room.getCurrentTemp() > room.getInitialTemp()) {
            newTemp = Math.max(newTemp - tempChange, room.getInitialTemp());
        }
        
        room.setCurrentTemp(newTemp);
        roomMapper.updateById(room);
    }
    
    /**
     * 计算温度变化
     */
    private double calculateTemperatureChange(String mode, String fanSpeed, long minutes) {
        double rate = 0;
        
        if (AcMode.COOLING.getCode().equals(mode)) {
            switch (fanSpeed) {
                case "HIGH": rate = COOLING_RATE_HIGH; break;
                case "MEDIUM": rate = COOLING_RATE_MEDIUM; break;
                case "LOW": rate = COOLING_RATE_LOW; break;
            }
        } else if (AcMode.HEATING.getCode().equals(mode)) {
            switch (fanSpeed) {
                case "HIGH": rate = HEATING_RATE_HIGH; break;
                case "MEDIUM": rate = HEATING_RATE_MEDIUM; break;
                case "LOW": rate = HEATING_RATE_LOW; break;
            }
        }
        
        return rate * minutes;
    }
    
    /**
     * 计算服务时长（分钟） - 按10秒实际时间=1分钟系统时间计算
     */
    private long calculateServiceMinutes(Room room) {
        if (room.getServiceStartTime() == null) return 0;
        // 实际秒数除以10 = 系统分钟数（10秒实际时间 = 1分钟系统时间）
        return Duration.between(room.getServiceStartTime(), LocalDateTime.now()).toSeconds() / 10;
    }
} 