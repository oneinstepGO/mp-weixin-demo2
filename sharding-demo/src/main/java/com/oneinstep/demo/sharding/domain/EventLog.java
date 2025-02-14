package com.oneinstep.demo.sharding.domain;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件日志表
 */
// 指定表名，仍然使用 逻辑表名
@Table("event_log")
@Data
public class EventLog {

    /**
     * 主键 使用 ShardingSphere 的雪花算法
     */
    @Id(keyType = KeyType.Generator, value = "snowflakeIdGenerator")
    private Long id;

    /**
     * 事件类型
     */
    private Integer eventType;

    /**
     * 事件内容
     */
    private String eventContent;

    /**
     * 逻辑删除字段
     */
    private Integer deleted;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
