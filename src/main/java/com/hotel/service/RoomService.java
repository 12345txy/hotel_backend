package com.hotel.service;

import com.hotel.entity.Room;

/**
 * 房间服务对象
 * 管理所有房间对象
 */
public interface RoomService {

    /**
     * 根据房间ID获取房间对象
     * @param roomId 房间ID
     * @return 房间对象
     */
    Room getRoomById(Long roomId);
}
