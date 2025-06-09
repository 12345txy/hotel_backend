package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房间Mapper接口
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    
    /**
     * 查询可用房间
     */
    @Select("SELECT * FROM rooms WHERE status = 'AVAILABLE'")
    List<Room> findAvailableRooms();
    

    
    /**
     * 根据房间ID查询房间
     */
    @Select("SELECT * FROM rooms WHERE id = #{roomId}")
    Room findByRoomId(Long roomId);
    
    /**
     * 查找已入住的房间
     */
    @Select("SELECT * FROM rooms WHERE status = 'OCCUPIED'")
    List<Room> findOccupiedRooms();
    
    /**
     * 查找等待时间超过阈值的房间
     */
    @Select("SELECT * FROM rooms WHERE waiting_start_time IS NOT NULL AND waiting_duration >= #{threshold}")
    List<Room> findWaitingRoomsOverThreshold(long threshold);
    
    /**
     * 查找相同风速中服务时间最长的房间
     */
    @Select("SELECT * FROM rooms WHERE fan_speed = #{fanSpeed} AND current_ac_id IS NOT NULL " +
            "AND id != #{excludeRoomId} AND service_duration IS NOT NULL " +
            "ORDER BY service_duration DESC LIMIT 1")
    Room findLongestServingRoomWithSameFanSpeed(String fanSpeed, Long excludeRoomId);
    
    /**
     * 查找正在回温的房间
     */
    @Select("SELECT * FROM rooms WHERE is_warming_back = true")
    List<Room> findWarmingBackRooms();
} 