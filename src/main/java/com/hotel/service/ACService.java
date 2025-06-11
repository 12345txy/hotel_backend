package com.hotel.service;

import com.hotel.entity.RoomRequest;

/**
 * 空调服务对象
 * 管理所有空调实例
 */
public interface ACService {
    RoomRequest startAC(Long roomId);
    void printStatus();
    boolean changeTemp(Long acId, Double targetTemp);
}
