package com.hotel.controller;

import com.hotel.dto.AcAdjustRequest;
import com.hotel.service.AcScheduleService;
import com.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 空调控制器
 */
@RestController
@RequestMapping("/api/ac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AcController {
    
    private final HotelService hotelService;
    private final AcScheduleService scheduleService;
    
    /**
     * 房间开启空调
     * @param roomId 房间ID
     * @param currentTemp 当前温度（可选，不提供则使用默认值）
     */
    @PostMapping("/room/{roomId}/start")
    public ResponseEntity<Map<String, String>> startAc(
            @PathVariable Long roomId,
            @RequestParam(required = false) Double currentTemp) {
        try {
            String result = hotelService.startAc(roomId, currentTemp);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 房间关闭空调
     */
    @PostMapping("/room/{roomId}/stop")
    public ResponseEntity<Map<String, String>> stopAc(@PathVariable Long roomId) {
        try {
            String result = hotelService.stopAc(roomId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 调整空调参数
     */
    @PutMapping("/room/{roomId}/adjust")
    public ResponseEntity<Map<String, String>> adjustAc(
            @PathVariable Long roomId,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String fanSpeed,
            @RequestParam(required = false) Double targetTemp) {
        try {
            String result = hotelService.adjustAc(roomId, mode, fanSpeed, targetTemp);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取调度队列状态
     */
    @GetMapping("/schedule/status")
    public ResponseEntity<?> getScheduleStatus() {
        return ResponseEntity.ok(scheduleService.getDetailedScheduleStatus());
    }
    
    /**
     * 获取指定房间的空调状态
     */
    @GetMapping("/room/{roomId}/status")
    public ResponseEntity<?> getRoomAcStatus(@PathVariable Long roomId) {
        // 暂时保留roomNumber的逻辑，后续可以进一步优化
        AcScheduleService.AcRequest request = scheduleService.getRoomRequest(roomId.intValue());
        if (request == null) {
            return ResponseEntity.ok(Map.of("message", "房间未开启空调"));
        }
        return ResponseEntity.ok(request);
    }
} 