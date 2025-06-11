package com.hotel.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

public class WaitingQueue extends BaseQueue{
    public WaitingQueue() {
        super();
        queue = new PriorityQueue<>(
                Comparator.comparing(RoomRequest::getFanSpeedPriority).reversed() // 风速高的优先（优先换入）
                        .thenComparing(RoomRequest::getWaitingTime, Comparator.reverseOrder()) // 等待时间长的优先（优先换入）
                        .thenComparing(RoomRequest::getRoomId)                     // 房间号小的优先（优先换入）
        );
    }

    public void enqueue (RoomRequest roomRequest) {
        roomRequest.setWaitingTime(LocalDateTime.now());
        roomRequest.setServingTime(null);
        roomRequest.setCurrentACId(null);
        roomRequest.setAcOn(false);
        super.enqueue(roomRequest);
    }

}
