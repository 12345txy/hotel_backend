package com.hotel.service;

import com.hotel.entity.Room;
import com.hotel.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务服务
 * 处理时间片轮转、温度监控和时长更新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleTaskService {
    
    private final RoomMapper roomMapper;
    private final AcScheduleService scheduleService;
    private final TemperatureService temperatureService;
    
    // 时间片轮转阈值：120秒
    private static final long TIME_SLICE_THRESHOLD = 120;
    
    /**
     * 每10秒执行一次的定时任务
     * 根据文档：测试时10秒等于系统的60秒
     */
    @Scheduled(fixedRate = 10000) // 10秒
    @Transactional
    public void executePeriodicTasks() {
        log.debug("执行定时任务：更新时长、检查温度、时间片轮转");
        
        // 1. 更新所有房间的服务/等待时长
        updateDurations();
        
        // 2. 更新房间温度
        updateTemperatures();
        
        // 3. 检查时间片轮转
        checkTimeSliceRotation();
        
        // 4. 处理回温检查
        handleWarmingBackCheck();
        
        // 5. 输出队列状态（每分钟一次）
        printQueueStatus();
    }
    
    /**
     * 更新所有房间的服务/等待时长
     */
    private void updateDurations() {
        List<Room> rooms = roomMapper.findOccupiedRooms();
        LocalDateTime now = LocalDateTime.now();
        
        for (Room room : rooms) {
            boolean updated = false;
            
            // 更新服务时长
            if (room.getServiceStartTime() != null) {
                long serviceDuration = Duration.between(room.getServiceStartTime(), now).toSeconds();
                room.setServiceDuration(serviceDuration);
                updated = true;
            }
            
            // 更新等待时长
            if (room.getWaitingStartTime() != null) {
                long waitingDuration = Duration.between(room.getWaitingStartTime(), now).toSeconds();
                room.setWaitingDuration(waitingDuration);
                updated = true;
            }
            
            if (updated) {
                roomMapper.updateById(room);
            }
        }
    }
    
    /**
     * 更新所有房间温度
     */
    private void updateTemperatures() {
        List<Room> rooms = roomMapper.findOccupiedRooms();
        for (Room room : rooms) {
            if (Boolean.TRUE.equals(room.getAcOn())) {
                temperatureService.updateRoomTemperature(room);
            }
        }
    }
    
    /**
     * 检查时间片轮转
     */
    private void checkTimeSliceRotation() {
        // 获取等待队列中等待时间>=120秒的房间
        List<Room> waitingRooms = roomMapper.findWaitingRoomsOverThreshold(TIME_SLICE_THRESHOLD);
        
        for (Room waitingRoom : waitingRooms) {
            // 检查是否可以进行时间片轮转
            checkAndPerformTimeSliceRotation(waitingRoom);
        }
    }
    
    /**
     * 检查并执行时间片轮转
     */
    @Transactional
    public void checkAndPerformTimeSliceRotation(Room waitingRoom) {
        // 找到服务队列中相同风速且服务时间最长的房间
        Room longestServingRoom = roomMapper.findLongestServingRoomWithSameFanSpeed(
            waitingRoom.getFanSpeed(), waitingRoom.getId()
        );
        
        if (longestServingRoom != null && 
            longestServingRoom.getServiceDuration() != null &&
            longestServingRoom.getServiceDuration() >= TIME_SLICE_THRESHOLD) {
            
            log.info("执行时间片轮转：房间{}(等待{}秒)替换房间{}(服务{}秒)", 
                waitingRoom.getId(), waitingRoom.getWaitingDuration(),
                longestServingRoom.getId(), longestServingRoom.getServiceDuration());
            
            // 执行轮转
            scheduleService.performTimeSliceRotation(waitingRoom, longestServingRoom);
        }
    }
    
    /**
     * 处理回温检查
     */
    private void handleWarmingBackCheck() {
        List<Room> warmingRooms = roomMapper.findWarmingBackRooms();
        for (Room room : warmingRooms) {
            temperatureService.handleWarmingBack(room);
        }
    }
    
    /**
     * 手动触发时间片检查（用于测试）
     */
    @Transactional
    public void triggerTimeSliceCheck() {
        log.info("手动触发时间片检查");
        updateDurations();
        checkTimeSliceRotation();
    }
    
    /**
     * 手动触发温度更新（用于测试）
     */
    @Transactional
    public void triggerTemperatureUpdate() {
        log.info("手动触发温度更新");
        updateTemperatures();
    }
    
    /**
     * 输出队列状态 - 每分钟（10秒实际时间）输出一次
     */
    private void printQueueStatus() {
        try {
            // 获取内存中的队列数据
            var serviceQueue = scheduleService.getServiceQueue();
            var waitingQueue = scheduleService.getWaitingQueue();
            
            // 服务队列最多3个位置（3台空调）
            StringBuilder serviceQueueStr = new StringBuilder("[");
            var serviceList = serviceQueue.stream().toList();
            for (int i = 0; i < 3; i++) {
                if (i > 0) serviceQueueStr.append(", ");
                if (i < serviceList.size()) {
                    var request = serviceList.get(i);
                    // 转换为显示时间：实际秒数 -> 显示分钟数（10秒实际 = 1分钟显示）
                    long displayTime = (request.getServiceTime() / 10) * 60; // 转换为60秒的倍数
                    serviceQueueStr.append(String.format("R%d/%d", request.getRoomNumber(), displayTime));
                } else {
                    serviceQueueStr.append("---");
                }
            }
            serviceQueueStr.append("]");
            
            // 等待队列最多2个位置（5个房间 - 3个服务中 = 2个等待）
            StringBuilder waitingQueueStr = new StringBuilder("[");
            var waitingList = waitingQueue.stream().toList();
            for (int i = 0; i < 2; i++) {
                if (i > 0) waitingQueueStr.append(", ");
                if (i < waitingList.size()) {
                    var request = waitingList.get(i);
                    // 转换为显示时间：实际秒数 -> 显示分钟数（10秒实际 = 1分钟显示）
                    long displayTime = (request.getWaitTime() / 10) * 60; // 转换为60秒的倍数
                    waitingQueueStr.append(String.format("R%d/%d", request.getRoomNumber(), displayTime));
                } else {
                    waitingQueueStr.append("---");
                }
            }
            waitingQueueStr.append("]");
            
            // 输出格式化的队列状态
            log.info("服务队列：{}      等待队列：{}", serviceQueueStr.toString(), waitingQueueStr.toString());
            
        } catch (Exception e) {
            log.error("输出队列状态失败: {}", e.getMessage());
        }
    }
} 