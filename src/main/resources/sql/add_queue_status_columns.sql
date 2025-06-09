-- 为现有数据库添加队列状态字段
USE hotel_ac_db;

-- 添加队列状态相关字段到rooms表
ALTER TABLE rooms
ADD COLUMN queue_status VARCHAR(20) DEFAULT 'NONE' COMMENT '队列状态：NONE-不在队列，SERVICE-服务队列，WAITING-等待队列' AFTER assigned_ac_number,
ADD COLUMN queue_position INT DEFAULT 0 COMMENT '在队列中的位置（1为队头）' AFTER queue_status,
ADD COLUMN queue_priority_score DECIMAL(10, 2) DEFAULT 0 COMMENT '队列优先级分数' AFTER queue_position;

-- 添加索引以提高查询性能
CREATE INDEX idx_queue_status ON rooms (queue_status);

CREATE INDEX idx_queue_position ON rooms (queue_status, queue_position);

-- 初始化现有数据的队列状态
UPDATE rooms
SET
    queue_status = 'NONE',
    queue_position = 0,
    queue_priority_score = 0
WHERE
    queue_status IS NULL;

SELECT 'Queue status columns added successfully!' AS result;