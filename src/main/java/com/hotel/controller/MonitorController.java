package com.hotel.controller;

import com.hotel.entity.*;
import com.hotel.service.ACService;
import com.hotel.service.ACScheduleService;
import com.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor")
@Slf4j
public class MonitorController {

    private final RoomService roomService;
    private final ACService acService;

    /**
     * 获取当前房间状态
     */
    @GetMapping("/roomstatus")
    public List<Map<String, Object>> getRoomStatus() {
        Map<Long, Room> roomMap = roomService.getAllRooms();
        List<Map<String, Object>> result = new ArrayList<>();

        log.info("🔍 当前房间状态监控如下：");
        log.info("当前房间数量: {}", roomMap.size());

        for (Map.Entry<Long, Room> entry : roomMap.entrySet()) {
            Long roomId = entry.getKey();
            Room room = entry.getValue();
            AirConditioner ac = acService.getACByRoomId(roomId);

            Map<String, Object> status = new LinkedHashMap<>();
            status.put("roomId", roomId);
            status.put("currentTemp", room.getCurrentTemp());
            status.put("defaultTemp", room.getDefaultTemp());

            if (ac != null) {
                status.put("targetTemp", ac.getTargetTemp());
                status.put("fanSpeed", ac.getFanSpeed());
                status.put("mode", ac.getMode());
                status.put("acOn", ac.getOn());
            } else {
                status.put("targetTemp", "空调未连接");
                status.put("fanSpeed", "空调未连接");
                status.put("mode", "空调未连接");
                status.put("acOn", false);
            }

            log.info("房间{} 状态: {}", roomId, status);
            result.add(status);
        }

        return result;
    }




    /**
     * 获取当前服务队列和等待队列状态
     */
    private final ACScheduleService acScheduleService;
    @GetMapping("/queuestatus")
    public Map<String, List<Map<String, Object>>> getQueueStatus() {
        List<RoomRequest> servingListRaw = acScheduleService.getServingQueue();
        List<RoomRequest> waitingListRaw = acScheduleService.getWaitingQueue();

        log.info("🌀 当前服务队列房间数: {}", servingListRaw.size());
        log.info("🌀 当前等待队列房间数: {}", waitingListRaw.size());

        List<Map<String, Object>> servingList = servingListRaw.stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("roomId", r.getRoomId());
                    map.put("fanSpeed", r.getFanSpeed());
                    map.put("servingTime", r.getServingTime());
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> waitingList = waitingListRaw.stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("roomId", r.getRoomId());
                    map.put("fanSpeed", r.getFanSpeed());
                    map.put("waitingTime", r.getWaitingTime());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("servingQueue", servingList);
        result.put("waitingQueue", waitingList);

        return result;
    }

}