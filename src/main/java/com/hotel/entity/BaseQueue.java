package com.hotel.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

@Data
public class BaseQueue {
    protected PriorityQueue<RoomRequest> queue;

    public BaseQueue() {
        this.queue = new PriorityQueue<>();
    }
    public void enqueue (RoomRequest roomRequest){
        queue.add(roomRequest);
    }

    public int size(){
        return queue.size();
    }

    public String getQueueInfo(){
        StringBuilder sb = new StringBuilder();
        for (RoomRequest roomRequest : queue) {
            sb.append(roomRequest.getAllInfo()).append("\n");
        }
        return sb.toString();
    }

    public RoomRequest dequeue(){
        return queue.poll();
    }

    public RoomRequest dequeue(Long roomId){
        RoomRequest result = null;
        List<RoomRequest> temp = new ArrayList<>();
        while (!queue.isEmpty()){
            RoomRequest roomRequest = queue.poll();
            if (!roomRequest.getRoomId().equals(roomId)){
                temp.add(roomRequest);
            }else{
                result = roomRequest;
            }
        }
        for (RoomRequest roomRequest : temp) {
            enqueue(roomRequest);
        }
        return result;
    }

    public String changeTemp(Long roomId, Double targetTemp) {
        RoomRequest roomRequest = dequeue(roomId);
        if (roomRequest != null) {
            roomRequest.setTargetTemp(targetTemp);
            enqueue(roomRequest);
            return "温度已调整";
        }else{
            return "不存在房间" + roomId + "的请求";
        }
    }


    public RoomRequest getRoomRequest(Long roomId){
        for (RoomRequest roomRequest : queue) {
            if (roomRequest.getRoomId().equals(roomId)) {
                return roomRequest;
            }
        }
        return null;
    }

    public boolean checkRoomId(Long roomId){
        for (RoomRequest roomRequest : queue) {
            if (roomRequest.getRoomId().equals(roomId)) {
                return true;
            }
        }
        return false;
    }
}
