package com.oneinstep.demo.sharding.controller;

import com.mybatisflex.core.query.QueryWrapper;
import com.oneinstep.demo.sharding.domain.EventLog;
import com.oneinstep.demo.sharding.generator.SnowflakeIdGenerator;
import com.oneinstep.demo.sharding.service.EventLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/event-logs")
@Slf4j
public class EventLogController {

    private static final LocalDateTime START_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

    private final EventLogService eventLogService;

    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    /**
     * 批量插入测试数据
     */
    @PostMapping("/batchInsertTest")
    public List<EventLog> batchInsertTest() {
        List<EventLog> logs = new ArrayList<>();
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

        // 创建测试数据，从2025-01-01开始，到2025-12-31结束，每天插入1条数据
        for (int i = 0; i < 365; i++) {
            EventLog eventLog = new EventLog();
            // 设置创建时间
            LocalDateTime createTime = LocalDateTime.of(2025, 1, 1, 0, 0).plusDays(i);
            eventLog.setCreateTime(createTime);

            // 根据创建时间生成ID
            long id = idGenerator.generateWithTime(createTime);
            eventLog.setId(id);

            eventLog.setEventType(1);
            eventLog.setEventContent("Test Event " + i);
            eventLog.setUpdateTime(createTime);

            // 验证ID中的时间戳
            String yearMonth = SnowflakeIdGenerator.extractYearMonth(id);
            String expectedYearMonth = createTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
            if (!yearMonth.equals(expectedYearMonth)) {
                log.warn("Time mismatch in ID. ID time: {}, Create time: {}", yearMonth, expectedYearMonth);
            }

            logs.add(eventLog);
        }

        eventLogService.saveBatch(logs);
        return logs;
    }

    /**
     * 按时间范围查询
     */
    @GetMapping("/query")
    @Transactional(readOnly = true)
    public List<EventLog> queryByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {

        // 注意不能用 gt 和 lt，因为 shardingsphere 不支持
        /**
         * 原因是：
         * 1. ShardingSphere 的 INTERVAL 分片算法主要是为了处理固定范围的查询场景
         * 2. between 操作符能让 ShardingSphere 精确计算需要查询的分片
         * 3. 而 > 和 < 这样的操作符会使 ShardingSphere 无法准确判断分片范围，可能导致查询所有分片
         */
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getCreateTime)
                .between(startTime.atStartOfDay(), endTime.plusDays(1).atStartOfDay().minusSeconds(1))
                .orderBy(EventLog::getCreateTime, true);

        return eventLogService.list(queryWrapper);
    }

    /**
     * 插入单条数据
     */
    @PostMapping
    public EventLog insert(@RequestBody EventLog eventLog) {
        eventLogService.save(eventLog);
        return eventLog;
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public EventLog getById(@PathVariable Long id) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getId).eq(id)
                .and(EventLog::getCreateTime)
                .between(START_TIME, END_TIME);
        return eventLogService.getOne(queryWrapper);
    }

    /**
     * 测试按时间范围删除
     */
    @DeleteMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    public String deleteByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getCreateTime)
                .between(startTime.atStartOfDay(), endTime.plusDays(1).atStartOfDay().minusSeconds(1));

        eventLogService.remove(queryWrapper);
        return "ok";
    }

    /**
     * 批量查询
     */
    @PostMapping("/batch-query")
    public List<EventLog> batchQuery(@RequestBody List<Long> ids) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getId).in(ids)
                .and(EventLog::getCreateTime)
                .between(START_TIME, END_TIME);
        return eventLogService.list(queryWrapper);
    }

    /**
     * 更新事件日志
     */
    @PutMapping("/{id}")
    public EventLog update(@PathVariable Long id, @RequestBody EventLog eventLog) {
        // 确保ID一致
        eventLog.setId(id);
        // 设置更新时间
        eventLog.setUpdateTime(LocalDateTime.now());

        boolean success = eventLogService.updateById(eventLog);
        if (!success) {
            throw new RuntimeException("Update failed, record not found: " + id);
        }
        return eventLogService.getById(id);
    }

    /**
     * 批量更新
     */
    @PutMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public List<EventLog> batchUpdate(@RequestBody List<EventLog> eventLogs) {
        // 设置更新时间
        LocalDateTime now = LocalDateTime.now();
        eventLogs.forEach(log -> log.setUpdateTime(now));

        eventLogService.updateBatch(eventLogs);
        return eventLogs;
    }

    /**
     * 条件查询
     */
    @GetMapping("/search")
    public List<EventLog> search(
            @RequestParam(required = false) Integer eventType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {

        QueryWrapper queryWrapper = QueryWrapper.create();

        // 添加事件类型条件
        if (eventType != null) {
            queryWrapper.where(EventLog::getEventType).eq(eventType);
        }

        // 添加时间范围条件
        if (startTime != null && endTime != null) {
            queryWrapper.and(EventLog::getCreateTime)
                    .between(startTime.atStartOfDay(), endTime.plusDays(1).atStartOfDay().minusSeconds(1));
        }

        queryWrapper.orderBy(EventLog::getCreateTime, true);
        return eventLogService.list(queryWrapper);
    }

}
