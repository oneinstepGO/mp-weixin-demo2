-- 创建 sharding_demo 数据库
CREATE DATABASE IF NOT EXISTS sharding_demo;
-- 使用 sharding_demo 数据库
USE sharding_demo;
-- 删除模版表
DROP TABLE IF EXISTS event_log_template;
-- 删除 1 到 12 月的表
DROP TABLE IF EXISTS event_log_202501;
DROP TABLE IF EXISTS event_log_202502;
DROP TABLE IF EXISTS event_log_202503;
DROP TABLE IF EXISTS event_log_202504;
DROP TABLE IF EXISTS event_log_202505;
DROP TABLE IF EXISTS event_log_202506;
DROP TABLE IF EXISTS event_log_202507;
DROP TABLE IF EXISTS event_log_202508;
DROP TABLE IF EXISTS event_log_202509;
DROP TABLE IF EXISTS event_log_202510;
DROP TABLE IF EXISTS event_log_202511;
DROP TABLE IF EXISTS event_log_202512;

-- 创建模版表
CREATE TABLE IF NOT EXISTS event_log_template
(
    `id`            BIGINT(20) PRIMARY KEY COMMENT 'ID',
    `event_type`    TINYINT(4)    NOT NULL DEFAULT '1' COMMENT '事件类型',
    `event_content` VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '事件内容',
    `create_time`   DATETIME      NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`       TINYINT(4)    NOT NULL DEFAULT '0' COMMENT '0-未删除 1-已删除',
    `status`        TINYINT(4)    NOT NULL DEFAULT '0' COMMENT '0-初始化 1-处理中 2-处理成功 3-处理失败',
    INDEX `idx_create_time` (`create_time`) USING BTREE
) COMMENT = '事件日志表' COLLATE = 'utf8mb4_general_ci'
                         ENGINE = InnoDB;
-- 使用存储过程 创建 1 到 12 月的表
DELIMITER // DROP PROCEDURE IF EXISTS create_event_log_tables;
CREATE PROCEDURE create_event_log_tables()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 12
        DO
            SET @table_name = CONCAT('event_log_2025', LPAD(i, 2, '0'));
            SET @create_table_sql = CONCAT(
                    'CREATE TABLE IF NOT EXISTS ',
                    @table_name,
                    ' LIKE event_log_template'
                                    );
            PREPARE stmt
                FROM @create_table_sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SET i = i + 1;
        END WHILE;
END //
DELIMITER ;
CALL create_event_log_tables();