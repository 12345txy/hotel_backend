package com.hotel.service.impl;

import com.hotel.entity.Room;
import com.hotel.entity.RoomConfig;
import com.hotel.mapper.RoomConfigMapper;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {
    private final RoomConfigMapper roomConfigMapper;

    @Value("${hotel.ac.room-count}")
    private int roomCount;

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
    public Room getRoomById(Long roomId) {
        if (roomId == null || roomId <= 0 || roomId > roomCount) {
            return null;
        }
        return rooms.get(roomId);
    }
}
