package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomConfig;
import com.hotel.mapper.RoomConfigMapper;
import com.hotel.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomConfigMapper roomConfigMapper;

    @Value("${hotel.ac.room-count}")
    private int roomCount;

    @Value("${hotel.ac.mode}")
    private int mode;

    private final Map<Long, Room> rooms;

    @Autowired
    public RoomServiceImpl(RoomConfigMapper roomConfigMapper) {
        this.roomConfigMapper = roomConfigMapper;
        this.rooms = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        for (long id = 1; id <= roomCount; id++) {
            RoomConfig roomConfig = roomConfigMapper.selectById(id);
            rooms.put( id, new Room(roomConfig));
        }
    }

    @Override
    public void printStatus() {
        for (Room room : rooms.values()) {
            log.info("房间{}状态: {}", room.getId(), room.getAllInfo());
        }
    }

    @Override
    public Room getRoomById(Long roomId) {
        if (roomId == null || roomId <= 0 || roomId > roomCount) {
            return null;
        }
        return rooms.get(roomId);
    }

    @Override
    public void coolingRoom(Long roomId, Double decreaseTemp) {
        Room room = getRoomById(roomId);
        double temp = room.getCurrentTemp() - decreaseTemp * mode;
        // 进行取整防止浮点数精度累计问题
        double rounded = Math.round(temp);
        double diff = Math.abs(rounded - temp);
        if (diff < 0.0101) {
            temp = rounded;
        }
        room.setCurrentTemp(temp);
    }

    @Override
    public void heatingRooms(List<Long> excludeRoomIds, Double increaseTemp) {
        for (Room room : rooms.values()){
            if (!excludeRoomIds.contains(room.getId())){
                double temp = room.getCurrentTemp() + increaseTemp * mode;
                if ((temp - room.getDefaultTemp()) * mode > 0){
                    temp = room.getDefaultTemp();
                }
                room.setCurrentTemp(temp);
            }
        }
    }
    @Override
    public Map<Long, Room> getAllRooms() {
        return Collections.unmodifiableMap(rooms);
    }

}
