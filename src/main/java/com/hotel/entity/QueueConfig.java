package com.hotel.entity;


import com.hotel.entity.ServingQueue;
import com.hotel.entity.WaitingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    @Bean
    public ServingQueue servingQueue() {
        return new ServingQueue(1); // 你可自定义 multiplier
    }

    @Bean
    public WaitingQueue waitingQueue() {
        return new WaitingQueue();
    }
}
