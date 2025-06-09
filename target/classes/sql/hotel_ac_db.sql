-- 酒店空调数据库建表脚本

CREATE DATABASE IF NOT EXISTS hotel_ac_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_ac_db;

-- 房间表
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(10) NOT NULL UNIQUE COMMENT '房间号',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '房间状态：AVAILABLE-可用，OCCUPIED-已入住，MAINTENANCE-维修中',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(100) COMMENT '客户姓名',
    check_in_time DATETIME COMMENT '入住时间',
    check_out_time DATETIME COMMENT '退房时间',

-- 空调相关字段
ac_on BOOLEAN DEFAULT FALSE COMMENT '空调是否开启',
ac_mode VARCHAR(20) COMMENT '空调模式：COOLING-制冷，HEATING-制热',
fan_speed VARCHAR(20) COMMENT '风速：LOW-低，MEDIUM-中，HIGH-高',
target_temp DECIMAL(4, 1) COMMENT '目标温度',
current_temp DECIMAL(4, 1) DEFAULT 26.0 COMMENT '当前温度',
initial_temp DECIMAL(4, 1) DEFAULT 26.0 COMMENT '初始温度',

-- 空调分配和服务状态
current_ac_id BIGINT COMMENT '当前分配的空调ID',
assigned_ac_number INT COMMENT '分配的空调编号',
ac_request_time DATETIME COMMENT '空调请求时间',
service_start_time DATETIME COMMENT '开始服务时间',
service_duration BIGINT DEFAULT 0 COMMENT '服务时长(秒)',
waiting_start_time DATETIME COMMENT '等待开始时间',
waiting_duration BIGINT DEFAULT 0 COMMENT '等待时长(秒)',

-- 队列状态字段（新增）
queue_status VARCHAR(20) DEFAULT 'NONE' COMMENT '队列状态：NONE-不在队列，SERVICE-服务队列，WAITING-等待队列',
queue_position INT DEFAULT 0 COMMENT '在队列中的位置（1为队头）',
queue_priority_score DECIMAL(10, 2) DEFAULT 0 COMMENT '队列优先级分数',

-- 回温相关

is_warming_back BOOLEAN DEFAULT FALSE COMMENT '是否正在回温',
    warming_start_time DATETIME COMMENT '回温开始时间',
    paused_mode VARCHAR(20) COMMENT '暂停前的模式',
    paused_fan_speed VARCHAR(20) COMMENT '暂停前的风速',
    paused_target_temp DECIMAL(4,1) COMMENT '暂停前的目标温度',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_room_number (room_number),
    INDEX idx_status (status),
    INDEX idx_customer_id (customer_id),
    INDEX idx_ac_on (ac_on),
    INDEX idx_current_ac_id (current_ac_id),
    INDEX idx_queue_status (queue_status),
    INDEX idx_queue_position (queue_status, queue_position)
) COMMENT '房间信息表';

-- ... existing code ...