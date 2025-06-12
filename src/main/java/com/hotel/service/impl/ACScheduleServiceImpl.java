package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomRequest;
import com.hotel.entity.ServingQueue;
import com.hotel.entity.WaitingQueue;
import com.hotel.service.ACScheduleService;
import com.hotel.service.ACService;
import com.hotel.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class ACScheduleServiceImpl implements ACScheduleService {
    private final ACService acService;
    private final RoomService roomService;

    @Value("${hotel.ac.total-count}")
    private int acCount;

    @Value("${hotel.ac.time-slice}")
    private int timeSlice;

    @Value("${hotel.ac.heating-rate}")
    private double heatingRate;

    @Value("${hotel.ac.mode}")
    private int mode;

    @Value("${hotel.ac.wake-up-temp}")
    private double wakeUpTemp;

    @Value("${hotel.time-multiplier}")
    private int timeMultiplier;

    private final ReentrantLock scheduleLock = new ReentrantLock();
    private ServingQueue servingQueue;
    private final WaitingQueue waitingQueue = new WaitingQueue();
    private final Map<Long, RoomRequest> sleepingRequests = new HashMap<>();


    @Autowired
    public ACScheduleServiceImpl(ACService acService, RoomService roomService) {
        this.acService = acService;
        this.roomService = roomService;
    }

    @PostConstruct
    public void init() {
        servingQueue = new ServingQueue(timeMultiplier);
    }

    private void printStatus() {
        log.info("开始打印状态");
        servingQueue.printStatus();
        waitingQueue.printStatus();
        // 打印休眠请求
        log.info("休眠请求：");
        for (Map.Entry<Long, RoomRequest> entry : sleepingRequests.entrySet()) {
            log.info(entry.getValue().getAllInfo());
        }
        acService.printStatus();
        roomService.printStatus();
    }

    /**
     * 实现时间片轮转和调度逻辑
     */
    private void schedule() {
        log.info("开始调度");
        // 移除服务队列中达到目标温度的房间
        log.info("移除服务队列中达到目标温度的房间");
        List<RoomRequest> servingRoomIds = servingQueue.getAllRequests();
        for (RoomRequest request : servingRoomIds) {
            Long roomId = request.getRoomId();
            Room room = roomService.getRoomById(roomId);
            // 如果当前温度小于等于目标温度，则移除出服务队列
            if ((room.getCurrentTemp() - request.getTargetTemp()) * mode <= 0) {
                RoomRequest sleepingRequest = servingQueue.dequeue(roomId);
                // todo: 记录详单
                request.sleep();
                sleepingRequests.put(roomId, sleepingRequest);
                log.info("房间{}已满足要求，进入休眠队列", roomId);
            }
        }
        // 查看是否有恢复请求的房间
        log.info("查看是否有恢复请求的房间");
        List<Long> wakeUpRoomIds = new ArrayList<>();
        for (RoomRequest request : sleepingRequests.values()) {
            Long roomId = request.getRoomId();
            Room room = roomService.getRoomById(roomId);
            // 如果当前温度大于等于目标温度+唤醒温度，则加入等待队列
            if ((room.getCurrentTemp() - request.getTargetTemp()) * mode >= wakeUpTemp) {
                wakeUpRoomIds.add(roomId);
                waitingQueue.enqueue(request);
                log.info("房间{}已加入等待队列", roomId);
            }
        }
        wakeUpRoomIds.forEach(sleepingRequests::remove);
        // 判断服务队列是否有空余
        log.info("服务队列剩余{}个位置", acCount - servingQueue.size());
        while (servingQueue.size() < acCount) {
            if (waitingQueue.size() > 0) {
                RoomRequest request = waitingQueue.dequeue();
                servingQueue.enqueue(request);
                log.info("房间{}已加入服务队列", request.getRoomId());
            } else {
                break;
            }
        }
        // 判断是否发生置换
        log.info("开始检查置换");
        while (waitingQueue.size() > 0) {
            RoomRequest request = waitingQueue.peek();
            if (servingQueue.checkReplace(request, timeSlice)) {
                waitingQueue.dequeue();
                RoomRequest waitingRequest = servingQueue.dequeue();
                // todo: 记录详单
                waitingQueue.enqueue(waitingRequest);
                servingQueue.enqueue(request);
                log.info("发生置换, 房间{}换入, 房间{}换出",
                        request.getRoomId(), waitingRequest.getRoomId());
            } else {
                break;
            }
        }
        // 更新空调状态
        log.info("更新空调状态");
        acService.update(servingQueue.getAllRequests());
        log.info("调度完成");
    }

    /**
     * 实现系统时钟
     * 每5秒执行一次
     */
    @Scheduled(fixedRateString = "${hotel.tick-time}")
    private void tick() {
        log.info("tick");
        scheduleLock.lock();

        // 进行调度
        schedule();
        // 打印状态
        printStatus();
        // 空调工作
        acService.tick();
        // 房间升温
        List<Long> servingRoomIds = servingQueue.getAllRoomIds();
        roomService.heatingRooms(servingRoomIds, heatingRate);

        scheduleLock.unlock();
    }


    public String startAC(Long roomId, Double currentTemp) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }
        String result;

        scheduleLock.lock();
        try {
            log.info("房间{}请求空调服务", roomId);
            // 检查是否已有请求
            RoomRequest oldRequest = servingQueue.getRoomRequest(roomId);
            if (oldRequest == null) {
                oldRequest = waitingQueue.getRoomRequest(roomId);
            }
            if (oldRequest != null) {
                result = "房间已请求空调服务";
            } else {
                // 根据队列创建请求
                if (servingQueue.size() < acCount) {
                    // 添加到服务队列
                    RoomRequest request = acService.startAC(roomId);
                    if (request == null) {
                        return "无法启动空调";
                    }
                    servingQueue.enqueue(request);
                    result = "空调已启动";
                } else {
                    // 添加到等待队列
                    RoomRequest request = acService.initRequest(roomId);
                    waitingQueue.enqueue(request);
                    result = "排队中";
                }
            }

        } finally {
            scheduleLock.unlock();
        }

        return result;
    }

    @Override
    public String changeTemp(Long roomId, Double targetTemp) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }
        String result;

        scheduleLock.lock();
        try {
            log.info("房间{}请求调整温度", roomId);
            if (servingQueue.checkRoomId(roomId)) {
                // 请求在服务队列
                RoomRequest request = servingQueue.getRoomRequest(roomId);
                if (!acService.changeTemp(request.getCurrentACId(), targetTemp)) {
                    result = "空调无法调整至目标温度";
                } else {
                    servingQueue.changeTemp(roomId, targetTemp);
                    result = "温度已调整";
                }
            } else if (waitingQueue.checkRoomId(roomId)) {
                // 请求在等待队列
                result = waitingQueue.changeTemp(roomId, targetTemp);
            } else if (sleepingRequests.get(roomId) != null) {
                RoomRequest request = sleepingRequests.get(roomId);
                request.setTargetTemp(targetTemp);
                result = "温度已调整";
            } else {
                result = "房间未开启空调";
            }

        } finally {
            scheduleLock.unlock();
        }
        return result;
    }

    public String changeFanSpeed(Long roomId, String fanSpeed) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }
        String result;

        scheduleLock.lock();
        try {
            log.info("房间{}请求调整风速", roomId);
            if (servingQueue.checkRoomId(roomId)) {
                // 请求在服务队列
                RoomRequest request = servingQueue.getRoomRequest(roomId);
                if (!acService.changeFanSpeed(request.getCurrentACId(), fanSpeed)) {
                    result = "空调无法调整至目标风速";
                } else {
                    servingQueue.changeFanSpeed(roomId, fanSpeed);
                    result = "风速已调整";
                }
            } else if (waitingQueue.checkRoomId(roomId)) {
                // 请求在等待队列
                result = waitingQueue.changeFanSpeed(roomId, fanSpeed);
            } else if (sleepingRequests.get(roomId) != null) {
                RoomRequest request = sleepingRequests.get(roomId);
                request.setFanSpeed(fanSpeed);
                result = "风速已调整";
            } else {
                result = "房间未开启空调";
            }

        } finally {
            scheduleLock.unlock();
        }
        return result;
    }

    public String stopAC(Long roomId) {
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            return "房间不存在";
        }
        String result;

        scheduleLock.lock();
        try {
            log.info("房间{}关闭空调服务", roomId);
            // 检查是否已有请求
            RoomRequest request = servingQueue.getRoomRequest(roomId);
            if (request != null) {
                // 从服务队列中移除并记录详单
                servingQueue.dequeue(roomId);
                // todo: 记录详单
            } else {
                // 从等待队列和休眠队列移除
                waitingQueue.dequeue(roomId);
                sleepingRequests.remove(roomId);
            }
            result = "房间空调已关闭";
        } finally {
            scheduleLock.unlock();
        }

        return result;
    }
}