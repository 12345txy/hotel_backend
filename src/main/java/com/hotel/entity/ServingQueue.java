package com.hotel.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

@Slf4j
public class ServingQueue extends BaseQueue{
    @Value("${hotel.time-multiplier}")
    int timeMultiplier;


    public ServingQueue() {
        super();
        queue = new PriorityQueue<>(
                Comparator.comparing(RoomRequest::getFanSpeedPriority)
                        .thenComparing(RoomRequest::getRequestTime, Comparator.reverseOrder())
                        .thenComparing(RoomRequest::getRoomId, Comparator.reverseOrder())
        );
    }

    public void printStatus(){
        log.info("服务队列: ");
        super.printStatus();
    }

    public void enqueue(RoomRequest roomRequest) {
        roomRequest.setServingTime(LocalDateTime.now());
        roomRequest.setWaitingTime(null);
        super.enqueue(roomRequest);
    }

    public boolean checkReplace(RoomRequest roomRequest, int timeSlice) {
        // 判断是否可以替换
        RoomRequest candidate = peek();
        if (candidate.getFanSpeedPriority() < roomRequest.getFanSpeedPriority()){
            // 优先级调度
            return true;
        }
        if (candidate.getFanSpeedPriority() == roomRequest.getFanSpeedPriority()){
            // 时间片调度
            Duration duration = Duration.between(LocalDateTime.now(), roomRequest.getWaitingTime());
            long seconds = duration.getSeconds() * timeMultiplier;
            seconds += (long) duration.getNano() * timeMultiplier / 1000000000L;
            // 允许误差范围为1秒
            return seconds - timeSlice >= -1 * timeMultiplier;
        }
        return false;
    }
}
