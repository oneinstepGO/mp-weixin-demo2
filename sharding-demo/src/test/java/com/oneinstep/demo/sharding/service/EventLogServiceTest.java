package com.oneinstep.demo.sharding.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.oneinstep.demo.sharding.domain.EventLog;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventLogServiceTest {

    @Resource
    private EventLogService eventLogService;

    @Test
    @Transactional
    void shouldInsertIntoCorrectTable() {
        // given
        EventLog eventLog = new EventLog();
        eventLog.setEventType(1);
        eventLog.setEventContent("Test event");
        eventLog.setCreateTime(LocalDateTime.of(2025, 3, 15, 10, 0)); // 应该插入到 event_log_202503

        // when
        boolean success = eventLogService.save(eventLog);

        // then
        assertTrue(success);
        assertNotNull(eventLog.getId());

        // 验证能查询到
        EventLog found = eventLogService.getById(eventLog.getId());
        assertNotNull(found);
        assertEquals(eventLog.getEventContent(), found.getEventContent());
    }

    @Test
    @Transactional
    void shouldQueryAcrossMonths() {
        // given
        List<EventLog> logs = new ArrayList<>();

        // 创建跨月份的测试数据
        EventLog log1 = createEventLog("Event 1", LocalDateTime.of(2025, 1, 15, 10, 0));
        EventLog log2 = createEventLog("Event 2", LocalDateTime.of(2025, 2, 15, 10, 0));
        EventLog log3 = createEventLog("Event 3", LocalDateTime.of(2025, 3, 15, 10, 0));

        logs.add(log1);
        logs.add(log2);
        logs.add(log3);

        eventLogService.saveBatch(logs);

        // when
        // 查询 2025-01-01 到 2025-03-31 的数据
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 3, 31, 23, 59);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getCreateTime).between(startTime, endTime)
                .orderBy(EventLog::getCreateTime, true);

        List<EventLog> results = eventLogService.list(queryWrapper);

        // then
        assertEquals(3, results.size());
        assertEquals("Event 1", results.get(0).getEventContent());
        assertEquals("Event 2", results.get(1).getEventContent());
        assertEquals("Event 3", results.get(2).getEventContent());
    }

    @Test
    @Transactional
    void shouldQueryByMonth() {
        // given
        List<EventLog> logs = new ArrayList<>();

        // 创建同一个月的多条数据
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 15, 10, 0);
        for (int i = 0; i < 5; i++) {
            EventLog log = createEventLog("Event " + i, baseTime.plusDays(i));
            logs.add(log);
        }

        eventLogService.saveBatch(logs);

        // when
        // 查询 2025-06 的数据
        LocalDateTime startTime = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 6, 30, 23, 59);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(EventLog::getCreateTime).between(startTime, endTime)
                .orderBy(EventLog::getCreateTime, true);

        List<EventLog> results = eventLogService.list(queryWrapper);

        // then
        assertEquals(5, results.size());
        // 验证数据按时间排序
        for (int i = 0; i < results.size(); i++) {
            assertEquals("Event " + i, results.get(i).getEventContent());
        }
    }

    private EventLog createEventLog(String content, LocalDateTime createTime) {
        EventLog log = new EventLog();
        log.setEventType(1);
        log.setEventContent(content);
        log.setCreateTime(createTime);
        return log;
    }
} 