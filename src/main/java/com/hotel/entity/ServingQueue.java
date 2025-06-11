package com.hotel.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ServingQueue extends BaseQueue{
    public ServingQueue() {
        super();
        queue = new PriorityQueue<>(
                Comparator.comparing(RoomRequest::getFanSpeedPriority)
                        .thenComparing(RoomRequest::getRequestTime, Comparator.reverseOrder())
                        .thenComparing(RoomRequest::getRoomId, Comparator.reverseOrder())
        );
    }

    public void enqueue(RoomRequest roomRequest) {
        roomRequest.setServingTime(LocalDateTime.now());
        roomRequest.setWaitingTime(null);
        super.enqueue(roomRequest);
    }
}
