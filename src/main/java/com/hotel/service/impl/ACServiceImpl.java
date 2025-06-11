package com.hotel.service.impl;

import com.hotel.entity.ACConfig;
import com.hotel.entity.AirConditioner;
import com.hotel.entity.RoomRequest;
import com.hotel.mapper.ACConfigMapper;
import com.hotel.service.ACService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ACServiceImpl implements ACService {

    private final ACConfigMapper acConfigMapper;

    @Value("${hotel.ac.total-count}")
    private int acCount;

    private final Map<Long, AirConditioner> acs;

    @Autowired
    public ACServiceImpl(ACConfigMapper acConfigMapper) {
        this.acConfigMapper = acConfigMapper;
        this.acs = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        ACConfig acConfig = acConfigMapper.selectById(1);
        for (long id = 1; id <= acCount; id++) {
            acs.put( id, new AirConditioner(id,acConfig));
        }
        log.info("初始化{}个空调", acCount);
    }

    public void printStatus() {
        for (AirConditioner ac : acs.values()) {
            log.info("空调{}状态: {}", ac.getId(), ac.getACAllInfo());
        }
    }

    @Override
    public RoomRequest startAC(Long roomId) {
        for (AirConditioner ac : acs.values()) {
            if (!ac.getOn()) {
                ac.init(roomId);
                RoomRequest request = new RoomRequest(roomId);
                request.setTargetTemp(ac.getDefaultTemp());
                request.setFanSpeed(ac.getDefaultSpeed());
                request.setCurrentACId(ac.getId());
                request.setAcOn(true);
                return request;
            }
        }
        return null;
    }

    @Override
    public boolean changeTemp(Long acId, Double targetTemp) {
        AirConditioner ac = acs.get(acId);
        if (targetTemp < ac.getMinTemp() || targetTemp > ac.getMaxTemp()){
            return false;
        }
        ac.setTargetTemp(targetTemp);
        return true;
    }
}
