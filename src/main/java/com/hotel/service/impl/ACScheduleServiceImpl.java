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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class ACScheduleServiceImpl implements ACScheduleService {
    private final ACService acService;
    private final RoomService roomService;

    @Value("${hotel.ac.total-count}")
    private int acCount;

    private final ReentrantLock scheduleLock = new ReentrantLock();
    private final ServingQueue servingQueue = new ServingQueue();
    private final WaitingQueue waitingQueue = new WaitingQueue();


    @Autowired
    public  ACScheduleServiceImpl(ACService acService, RoomService roomService) {
        this.acService = acService;
        this.roomService = roomService;
    }

    //  每5秒打印一次队列状态
    @Scheduled(fixedRate = 5000)
    private void printStatus() {
        log.info("服务队列: {}", servingQueue.getQueueInfo());
        log.info("等待队列: {}", waitingQueue.getQueueInfo());
        acService.printStatus();
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
                if (servingQueue.size() < acCount){
                    // 添加到服务队列
                    RoomRequest request = acService.startAC(roomId);
                    if (request == null) {
                        return "无法启动空调";
                    }
                    servingQueue.enqueue(request);
                    result = "空调已启动";
                }else{
                    // 添加到等待队列
                    RoomRequest request = new RoomRequest(roomId);
                    waitingQueue.enqueue(request);
                    result = "排队中";
                }
            }

        } finally {
            scheduleLock.unlock();
        }

        return  result;
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
            if (servingQueue.checkRoomId(roomId)){
                // 请求在服务队列
                RoomRequest request = servingQueue.getRoomRequest(roomId);
                if (!acService.changeTemp(request.getCurrentACId(), targetTemp)){
                    result = "空调无法调整至目标温度";
                }else{
                    servingQueue.changeTemp(roomId, targetTemp);
                    result = "温度已调整";
                }
            } else if (waitingQueue.checkRoomId(roomId)){
                // 请求在等待队列
                result = waitingQueue.changeTemp(roomId, targetTemp);
            } else {
                result = "房间未开启空调";
            }

        } finally {
            scheduleLock.unlock();
        }
        return result;
    }


//    /**
//     * 调整房间空调参数
//     */
//    public String adjustAcRequest(Integer roomNumber, String fanSpeed, String mode, Double targetTemp) {
//        scheduleLock.lock();
//        try {
//            log.info("房间{}调整空调参数: 风速={}, 模式={}, 目标温度={}", roomNumber, fanSpeed, mode, targetTemp);
//
//            com.hotel.service.ACScheduleService.AcRequest request = roomRequestMap.get(roomNumber);
//            if (request == null) {
//                return "房间未开启空调";
//            }
//
//            // 更新参数
//            if (fanSpeed != null) {
//                request.setFanSpeed(fanSpeed);
//            }
//            if (mode != null) {
//                request.setMode(mode);
//            }
//            if (targetTemp != null) {
//                request.setTargetTemp(targetTemp);
//            }
//
//            // 重新调度
//            return processSchedule();
//
//        } finally {
//            scheduleLock.unlock();
//        }
//    }
//
//    /**
//     * 移除空调请求（关机或达到目标温度）
//     */
//    public String removeAcRequest(Integer roomNumber, String reason) {
//        scheduleLock.lock();
//        try {
//            log.info("房间{}释放空调资源，原因: {}", roomNumber, reason);
//
//            com.hotel.service.ACScheduleService.AcRequest request = roomRequestMap.remove(roomNumber);
//            if (request == null) {
//                return "房间未开启空调";
//            }
//
//            // 从队列中移除
//            serviceQueue.remove(request);
//            waitingQueue.remove(request);
//
//            // 重新调度
//            return processSchedule();
//
//        } finally {
//            scheduleLock.unlock();
//        }
//    }
//
//    /**
//     * 处理调度逻辑 - 利用PriorityQueue特性实现高效调度
//     */
//    private String processSchedule() {
//        // 更新所有请求的时间
//        updateAllRequestTimes();
//
//        // 重新构建队列以反映时间变化（因为时间变化会影响优先级）
//        rebuildQueuesWithUpdatedPriorities();
//
//        // 检查时间片轮转
//        checkTimeSliceRotation();
//
//        // 同步队列状态到数据库 ⭐关键！
//        syncQueueStatusToDatabase();
//
//        return buildScheduleResult();
//    }
//
//    /**
//     * 添加新请求到适当的队列 - 利用队头元素特性
//     */
//    private void addNewRequest(com.hotel.service.ACScheduleService.AcRequest request) {
//        if (serviceQueue.size() < totalAcCount) {
//            // 服务队列未满，直接加入服务队列
//            assignAcToRequest(request);
//            serviceQueue.offer(request);  // 添加到服务队列
//        } else {
//            // 服务队列已满，检查是否可以抢占（队头就是最容易被换出的）
//            com.hotel.service.ACScheduleService.AcRequest lowestPriorityInService = serviceQueue.peek();
//
//            if (lowestPriorityInService != null &&
//                    request.getFanSpeedPriority() > lowestPriorityInService.getFanSpeedPriority()) {
//                // 高优先级抢占低优先级
//                performPrioritySchedule(request, lowestPriorityInService);
//            } else {
//                // 加入等待队列
//                addToWaitingQueue(request);
//            }
//        }
//    }
//
//    /**
//     * 重新构建队列以反映优先级变化
//     * 注意：由于时间会影响优先级排序，需要重建队列
//     */
//    private void rebuildQueuesWithUpdatedPriorities() {
//        // 保存当前队列中的请求
//        List<com.hotel.service.ACScheduleService.AcRequest> currentServiceRequests = new ArrayList<>(serviceQueue);
//        List<com.hotel.service.ACScheduleService.AcRequest> currentWaitingRequests = new ArrayList<>(waitingQueue);
//
//        // 清空队列
//        serviceQueue.clear();
//        waitingQueue.clear();
//
//        // 重新添加服务队列的请求（PriorityQueue会自动重新排序）
//        serviceQueue.addAll(currentServiceRequests);
//
//        // 重新添加等待队列的请求
//        waitingQueue.addAll(currentWaitingRequests);
//    }
//
//    /**
//     * 执行优先级调度 - 利用队列的poll()操作
//     */
//    private void performPrioritySchedule(com.hotel.service.ACScheduleService.AcRequest highPriorityRequest, com.hotel.service.ACScheduleService.AcRequest lowPriorityRequest) {
//        log.info("优先级调度：房间{}(风速{})抢占房间{}(风速{})",
//                highPriorityRequest.getRoomId(), highPriorityRequest.getFanSpeed(),
//                lowPriorityRequest.getRoomId(), lowPriorityRequest.getFanSpeed());
//
//        // 使用poll()移除队头的低优先级请求（最容易被换出的）
//        serviceQueue.poll();
//
//        // 重置时间并交换
//        lowPriorityRequest.setServiceStartTime(null);
//        lowPriorityRequest.setServiceTime(0L);
//        highPriorityRequest.setWaitTime(0L);
//
//        // 分配空调给高优先级请求
//        assignAcToRequest(highPriorityRequest);
//        serviceQueue.offer(highPriorityRequest);  // 添加到服务队列
//
//        // 低优先级请求加入等待队列
//        addToWaitingQueue(lowPriorityRequest);
//    }
//
//    /**
//     * 分配空调给请求
//     */
//    @Transactional
//    private void assignAcToRequest(com.hotel.service.ACScheduleService.AcRequest request) {
//        request.setInService(true);
//        if (request.getServiceStartTime() == null) {
//            request.setServiceStartTime(LocalDateTime.now());
//        }
//        request.setServiceTime(0L);
//
//        // 分配一个可用的空调
//        Integer acNumber = findAvailableAc();
//        if (acNumber != null) {
//            request.setAcId((long) acNumber);
//
//            // 更新空调表
//            AirConditioner ac = acMapper.findByAcNumber(acNumber);
//            if (ac != null) {
//                ac.setServingRoomId(request.getRoomId());
//                ac.setOn(true);
//                ac.setMode(request.getMode());
//                ac.setFanSpeed(request.getFanSpeed());
//                ac.setTargetTemp(request.getTargetTemp());
//                ac.setRequestTime(request.getRequestTime());
//                ac.setServiceStartTime(request.getServiceStartTime());
//                acMapper.updateById(ac);
//            }
//
//            // 更新房间表
//            Room room = roomMapper.selectById(request.getRoomId());
//            if (room != null) {
//                room.setCurrentAcId((long) acNumber);
//                room.setAssignedAcNumber(acNumber);
//                room.setServiceStartTime(request.getServiceStartTime());
//                roomMapper.updateById(room);
//            }
//        }
//
//        log.info("房间{}分配到空调{}服务", request.getRoomNumber(), acNumber);
//    }
//
//    /**
//     * 查找可用的空调 - 基于数据库实际状态检查
//     */
//    private Integer findAvailableAc() {
//        for (int i = 1; i <= totalAcCount; i++) {
//            final int acNumber = i;
//
//            // 检查数据库中是否有房间正在使用这个空调
//            QueryWrapper<Room> wrapper = new QueryWrapper<>();
//            wrapper.eq("current_ac_id", acNumber)
//                    .eq("ac_on", true)
//                    .eq("is_warming_back", false);  // 回温中的房间不算占用
//            List<Room> roomsUsingThisAc = roomMapper.selectList(wrapper);
//
//            if (roomsUsingThisAc.isEmpty()) {
//                return acNumber;
//            }
//        }
//        return null; // 所有空调都在使用中
//    }
//
//    /**
//     * 添加到等待队列
//     */
//    @Transactional
//    private void addToWaitingQueue(com.hotel.service.ACScheduleService.AcRequest request) {
//        request.setInService(false);
//        request.setWaitTime(0L);
//
//        // 如果之前分配了空调，需要释放
//        if (request.getAcId() != null) {
//            releaseAc(request.getAcId().intValue(), request.getRoomId());
//            request.setAcId(null);
//        }
//
//        // 使用offer()自动维护等待队列的优先级顺序
//        waitingQueue.offer(request);
//        log.info("房间{}加入等待队列", request.getRoomNumber());
//    }
//
//    /**
//     * 释放空调资源
//     */
//    @Transactional
//    private void releaseAc(Integer acNumber, Long roomId) {
//        // 更新空调表
//        AirConditioner ac = acMapper.findByAcNumber(acNumber);
//        if (ac != null) {
//            ac.setServingRoomId(null);
//            ac.setOn(false);
//            ac.setMode(null);
//            ac.setFanSpeed(null);
//            ac.setTargetTemp(null);
//            ac.setRequestTime(null);
//            ac.setServiceStartTime(null);
//            acMapper.updateById(ac);
//        }
//
//        // 更新房间表
//        Room room = roomMapper.selectById(roomId);
//        if (room != null) {
//            room.setCurrentAcId(null);
//            room.setAssignedAcNumber(null);
//            room.setServiceStartTime(null);
//            roomMapper.updateById(room);
//        }
//
//        log.info("释放空调{}，房间{}", acNumber, roomId);
//    }
//
//    /**
//     * 同步队列状态到数据库 - 解决内存队列与数据库不同步问题 ⭐核心方法
//     */
//    @Transactional
//    private void syncQueueStatusToDatabase() {
//        try {
//            // 1. 重置所有房间的队列状态
//            QueryWrapper<Room> resetWrapper = new QueryWrapper<>();
//            resetWrapper.eq("ac_on", true);
//
//            Room resetRoom = new Room();
//            resetRoom.setQueueStatus(QueueStatus.NONE.getCode());
//            resetRoom.setQueuePosition(0);
//            resetRoom.setQueuePriorityScore(0.0);
//
//            roomMapper.update(resetRoom, resetWrapper);
//
//            // 2. 同步服务队列状态
//            int servicePosition = 1;
//            for (com.hotel.service.ACScheduleService.AcRequest request : serviceQueue) {
//                Room room = roomMapper.selectById(request.getRoomId());
//                if (room != null) {
//                    room.setQueueStatus(QueueStatus.SERVICE.getCode());
//                    room.setQueuePosition(servicePosition++);
//                    room.setQueuePriorityScore((double) request.getFanSpeedPriority());
//                    roomMapper.updateById(room);
//                }
//            }
//
//            // 3. 同步等待队列状态
//            int waitingPosition = 1;
//            for (com.hotel.service.ACScheduleService.AcRequest request : waitingQueue) {
//                Room room = roomMapper.selectById(request.getRoomId());
//                if (room != null) {
//                    room.setQueueStatus(QueueStatus.WAITING.getCode());
//                    room.setQueuePosition(waitingPosition++);
//                    room.setQueuePriorityScore((double) request.getFanSpeedPriority());
//                    roomMapper.updateById(room);
//                }
//            }
//
//            log.debug("队列状态已同步到数据库：服务队列{}个，等待队列{}个",
//                    serviceQueue.size(), waitingQueue.size());
//
//        } catch (Exception e) {
//            log.error("同步队列状态到数据库失败: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 检查时间片轮转 - 完全利用队头元素，无需遍历
//     */
//    public void checkTimeSliceRotation() {
//        if (waitingQueue.isEmpty() || serviceQueue.isEmpty()) {
//            return;
//        }
//
//        // 队头就是最优先换入的等待请求
//        com.hotel.service.ACScheduleService.AcRequest highestWaitingRequest = waitingQueue.peek();
//
//        if (highestWaitingRequest != null && highestWaitingRequest.getWaitTime() >= timeSliceSeconds) {
//            // 队头就是最容易被换出的服务请求
//            com.hotel.service.ACScheduleService.AcRequest lowestServiceRequest = serviceQueue.peek();
//
//            // 检查是否为相同风速（时间片轮转的条件）
//            if (lowestServiceRequest != null &&
//                    lowestServiceRequest.getFanSpeedPriority() == highestWaitingRequest.getFanSpeedPriority()) {
//
//                // 时间片轮转
//                log.info("时间片轮转: 房间{}(等待{}秒)替换房间{}(服务{}秒)",
//                        highestWaitingRequest.getRoomNumber(), highestWaitingRequest.getWaitTime(),
//                        lowestServiceRequest.getRoomNumber(), lowestServiceRequest.getServiceTime());
//
//                // 直接使用poll()移除队头元素
//                waitingQueue.poll(); // 移除最优先换入的
//                serviceQueue.poll(); // 移除最容易被换出的
//
//                // 重置时间 - 被换入的房间服务时长从0开始，被换出的房间等待时长从0开始
//                lowestServiceRequest.setServiceStartTime(null);
//                lowestServiceRequest.setServiceTime(0L);
//                highestWaitingRequest.setWaitTime(0L);
//
//                // 使用offer()自动维护优先级
//                assignAcToRequest(highestWaitingRequest);
//                addToWaitingQueue(lowestServiceRequest);
//            }
//        }
//    }
//
//    /**
//     * 更新所有请求的时间
//     */
//    private void updateAllRequestTimes() {
//        roomRequestMap.values().forEach(request -> {
//            if (request.isInService()) {
//                request.updateServiceTime();
//            } else {
//                request.updateWaitTime();
//            }
//        });
//    }
//
//    /**
//     * 构建调度结果
//     */
//    private String buildScheduleResult() {
//        StringBuilder result = new StringBuilder();
//        result.append("调度结果:\n");
//        result.append("服务队列: ");
//        serviceQueue.forEach(req ->
//                result.append("R").append(req.getRoomNumber()).append("/").append(req.getServiceTime()).append(" "));
//        result.append("\n等待队列: ");
//        waitingQueue.forEach(req ->
//                result.append("R").append(req.getRoomNumber()).append("/").append(req.getWaitTime()).append(" "));
//
//        return result.toString();
//    }
//
//    /**
//     * 获取当前服务队列
//     */
//    public PriorityQueue<com.hotel.service.ACScheduleService.AcRequest> getServiceQueue() {
//        return new PriorityQueue<>(serviceQueue);
//    }
//
//    /**
//     * 获取当前等待队列
//     */
//    public PriorityQueue<com.hotel.service.ACScheduleService.AcRequest> getWaitingQueue() {
//        return new PriorityQueue<>(waitingQueue);
//    }
//
//    /**
//     * 获取房间的请求状态
//     */
//    public com.hotel.service.ACScheduleService.AcRequest getRoomRequest(Integer roomNumber) {
//        return roomRequestMap.get(roomNumber);
//    }
//
//    /**
//     * 获取详细的调度状态信息 - 基于数据库查询（已同步）
//     */
//    public Map<String, Object> getDetailedScheduleStatus() {
//        Map<String, Object> result = new HashMap<>();
//
//        try {
//            // 查询服务队列（按位置排序）
//            QueryWrapper<Room> serviceWrapper = new QueryWrapper<>();
//            serviceWrapper.eq("queue_status", QueueStatus.SERVICE.getCode())
//                    .orderByAsc("queue_position");
//            List<Room> serviceRooms = roomMapper.selectList(serviceWrapper);
//
//            // 查询等待队列（按位置排序）
//            QueryWrapper<Room> waitingWrapper = new QueryWrapper<>();
//            waitingWrapper.eq("queue_status", QueueStatus.WAITING.getCode())
//                    .orderByAsc("queue_position");
//            List<Room> waitingRooms = roomMapper.selectList(waitingWrapper);
//
//            // 转换为展示格式
//            List<Map<String, Object>> serviceQueueDetails = serviceRooms.stream()
//                    .map(this::createRoomInfo)
//                    .collect(Collectors.toList());
//
//            List<Map<String, Object>> waitingQueueDetails = waitingRooms.stream()
//                    .map(this::createRoomInfo)
//                    .collect(Collectors.toList());
//
//            result.put("serviceQueue", serviceQueueDetails);
//            result.put("waitingQueue", waitingQueueDetails);
//            result.put("timestamp", LocalDateTime.now());
//            result.put("totalAcCount", totalAcCount);
//
//            log.debug("获取队列状态：服务队列{}个，等待队列{}个",
//                    serviceQueueDetails.size(), waitingQueueDetails.size());
//
//        } catch (Exception e) {
//            log.error("获取调度状态失败: {}", e.getMessage());
//            result.put("serviceQueue", new ArrayList<>());
//            result.put("waitingQueue", new ArrayList<>());
//            result.put("timestamp", LocalDateTime.now());
//            result.put("totalAcCount", totalAcCount);
//        }
//
//        return result;
//    }
//
//    /**
//     * 基于AcRequest和Room创建房间信息（权威数据源）
//     */
//    private Map<String, Object> createRoomInfoFromRequest(com.hotel.service.ACScheduleService.AcRequest request, Room room) {
//        Map<String, Object> item = new HashMap<>();
//        item.put("roomId", request.getRoomId());
//        item.put("roomNumber", request.getRoomNumber());
//        item.put("fanSpeed", request.getFanSpeed());
//        item.put("mode", request.getMode());
//        item.put("targetTemp", request.getTargetTemp());
//        item.put("currentTemp", room.getCurrentTemp());
//        item.put("acId", request.getAcId());
//        item.put("inService", request.isInService());
//
//        // 使用Request中的时间数据（更准确）
//        long serviceTime = request.getServiceTime() != null ? request.getServiceTime() : 0L;
//        long waitTime = request.getWaitTime() != null ? request.getWaitTime() : 0L;
//
//        // 转换为显示时间：实际秒数/10 = 显示分钟数
//        long displayTime = request.isInService() ? serviceTime / 10 : waitTime / 10;
//
//        item.put("displayTime", Math.max(0, displayTime));
//        item.put("serviceTime", serviceTime);
//        item.put("waitTime", waitTime);
//
//        return item;
//    }
//
//    /**
//     * 根据Room实体创建房间信息（遗留方法）
//     */
//    private Map<String, Object> createRoomInfo(Room room) {
//        Map<String, Object> item = new HashMap<>();
//        item.put("roomId", room.getId());
//        item.put("roomNumber", room.getId().intValue());
//        item.put("fanSpeed", room.getFanSpeed());
//        item.put("mode", room.getAcMode());
//        item.put("targetTemp", room.getTargetTemp());
//        item.put("currentTemp", room.getCurrentTemp()); // 添加当前温度
//        item.put("acId", room.getCurrentAcId());
//        item.put("inService", room.getCurrentAcId() != null);
//
//        // 直接使用数据库中定时任务更新的时长数据（10秒实际时间 = 1分钟显示时间）
//        long serviceTime = room.getServiceDuration() != null ? room.getServiceDuration() : 0L;
//        long waitTime = room.getWaitingDuration() != null ? room.getWaitingDuration() : 0L;
//
//        // 转换为显示时间：10秒实际时间 = 1分钟显示时间
//        long displayTime = 0;
//        if (room.getCurrentAcId() != null) {
//            // 服务中 - 使用服务时长
//            displayTime = serviceTime / 10; // 10秒=1分钟
//        } else {
//            // 等待中 - 使用等待时长
//            displayTime = waitTime / 10; // 10秒=1分钟
//        }
//
//        item.put("displayTime", Math.max(0, displayTime));
//        item.put("serviceTime", serviceTime);
//        item.put("waitTime", waitTime);
//
//        return item;
//    }
//
//    /**
//     * 格式化队列项目信息
//     */
//    private Map<String, Object> formatQueueItem(com.hotel.service.ACScheduleService.AcRequest request) {
//        Map<String, Object> item = new HashMap<>();
//        item.put("roomId", request.getRoomId());
//        item.put("roomNumber", request.getRoomNumber());
//        item.put("fanSpeed", request.getFanSpeed());
//        item.put("mode", request.getMode());
//        item.put("targetTemp", request.getTargetTemp());
//
//        // 确保时间字段不为null
//        Long serviceTime = request.getServiceTime() != null ? request.getServiceTime() : 0L;
//        Long waitTime = request.getWaitTime() != null ? request.getWaitTime() : 0L;
//
//        item.put("serviceTime", serviceTime);
//        item.put("waitTime", waitTime);
//        item.put("inService", request.isInService());
//        item.put("acId", request.getAcId());
//
//        // 计算显示时间（秒转分钟），确保不为null
//        long displayTime = request.isInService() ?
//                serviceTime / 60 : waitTime / 60;
//        item.put("displayTime", displayTime);
//
//        return item;
//    }
//
//    /**
//     * 执行时间片轮转（基于数据库的房间实体）
//     */
//    @Transactional
//    public synchronized void performTimeSliceRotation(Room waitingRoom, Room servingRoom) {
//        log.info("执行时间片轮转：房间{}替换房间{}", waitingRoom.getId(), servingRoom.getId());
//
//        // 1. 为等待房间分配空调
//        Long acId = servingRoom.getCurrentAcId();
//        waitingRoom.setCurrentAcId(acId)
//                .setServiceStartTime(LocalDateTime.now())
//                .setServiceDuration(0L)
//                .setWaitingStartTime(null)
//                .setWaitingDuration(0L);
//
//        // 2. 将服务房间移到等待队列
//        servingRoom.setCurrentAcId(null)
//                .setServiceStartTime(null)
//                .setServiceDuration(0L)
//                .setWaitingStartTime(LocalDateTime.now())
//                .setWaitingDuration(0L);
//
//        // 3. 更新数据库
//        roomMapper.updateById(waitingRoom);
//        roomMapper.updateById(servingRoom);
//
//        // 4. 生成详单
//        createBillDetail(servingRoom, "时间片轮转-移出服务");
//        createBillDetail(waitingRoom, "时间片轮转-开始服务");
//
//        log.info("时间片轮转完成");
//    }
//
//    /**
//     * 创建账单详单
//     */
//    private void createBillDetail(Room room, String detailType) {
//        try {
//            if (room.getServiceStartTime() == null) return;
//
//            long duration = Duration.between(room.getServiceStartTime(), LocalDateTime.now()).toSeconds() / 60;
//            if (duration <= 0) return;
//
//            double rate = FanSpeed.valueOf(room.getFanSpeed()).getRate();
//            double cost = duration * rate;
//
//            BillDetail detail = new BillDetail();
//            detail.setBillId(null) // 退房时会更新
//                    .setRoomId(room.getId())
//                    .setRoomId(room.getId())
//                    .setCustomerId(room.getCustomerId())
//                    .setAcId(room.getCurrentAcId())
//                    .setAcMode(room.getAcMode())
//                    .setFanSpeed(room.getFanSpeed())
//                    .setStartTime(room.getServiceStartTime())
//                    .setEndTime(LocalDateTime.now())
//                    .setDuration(duration)
//                    .setCost(cost)
//                    .setRate(rate)
//                    .setDetailType(detailType)
//                    .setCreateTime(LocalDateTime.now());
//
//            billDetailMapper.insert(detail);
//            log.info("生成详单：房间{}, 类型{}, 时长{}分钟, 费用{}元",
//                    room.getId(), detailType, duration, cost);
//        } catch (Exception e) {
//            log.error("生成详单失败：房间{}, 错误: {}", room.getId(), e.getMessage());
//        }
//    }
}
