package com.oneinstep.rule.core.service;

import com.oneinstep.rule.core.config.RuleLogProperties;
import com.oneinstep.rule.core.model.RuleExecutionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleExecutionLogServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private RuleLogProperties logProperties;

    private RuleExecutionLogService logService;
    private File logFile;

    @BeforeEach
    void setUp() {
        // 配置临时日志文件
        logFile = tempDir.resolve("rule-execution.log").toFile();
        when(logProperties.getLogDir()).thenReturn(tempDir.toString());
        when(logProperties.getExecutionLogFile()).thenReturn("rule-execution.log");

        logService = new RuleExecutionLogService(logProperties);
    }

    @Test
    void testLogExecution() throws IOException {
        // 准备测试数据
        RuleExecutionLog log = createTestLog(true);

        // 执行测试
        logService.logExecution(log);

        // 验证日志文件内容
        List<String> logLines = Files.readAllLines(logFile.toPath());
        assertEquals(1, logLines.size());
        String logLine = logLines.getFirst();

        // 验证日志内容
        assertTrue(logLine.contains(log.getRuleGroup()));
        assertTrue(logLine.contains(log.getRuleId()));
        assertTrue(logLine.contains("Success: true"));
    }

    @Test
    void testLogExecutionWithError() throws IOException {
        // 准备测试数据
        RuleExecutionLog log = createTestLog(false);
        log.setErrorMessage("Test error message");

        // 执行测试
        logService.logExecution(log);

        // 验证日志文件内容
        List<String> logLines = Files.readAllLines(logFile.toPath());
        assertEquals(1, logLines.size());
        String logLine = logLines.getFirst();

        // 验证错误信息
        assertTrue(logLine.contains("Success: false"));
        assertTrue(logLine.contains("Test error message"));
    }

    @Test
    void testConcurrentLogExecution() throws InterruptedException {
        // 准备测试数据
        int threadCount = 10;
        int logsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 创建线程池
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {

            // 并发执行日志记录
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < logsPerThread; j++) {
                            logService.logExecution(createTestLog(true));
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            assertTrue(latch.await(30, TimeUnit.SECONDS));
            executorService.shutdown();
        }

        // 验证统计信息
        Long avgTime = logService.getAverageExecutionTime("testGroup");
        assertNotNull(avgTime);
        assertTrue(avgTime > 0);
    }

    @Test
    void testQueueOverflow() {
        // 快速提交超过队列容量的日志
        for (int i = 0; i < 11000; i++) {
            logService.logExecution(createTestLog(true));
        }

        // 验证统计信息仍然正确
        Long avgTime = logService.getAverageExecutionTime("testGroup");
        assertNotNull(avgTime);
        assertTrue(avgTime > 0);
    }

    @Test
    void testExecutionStats() {
        // 准备测试数据
        RuleExecutionLog log1 = createTestLog(true);
        log1.setExecuteDuration(100L);

        RuleExecutionLog log2 = createTestLog(true);
        log2.setExecuteDuration(200L);

        // 执行测试
        logService.logExecution(log1);
        logService.logExecution(log2);

        // 验证平均执行时间
        Long avgTime = logService.getAverageExecutionTime("testGroup");
        assertEquals(150L, avgTime);
    }

    private RuleExecutionLog createTestLog(boolean success) {
        return RuleExecutionLog.builder()
                .id(UUID.randomUUID().toString())
                .ruleGroup("testGroup")
                .ruleId("testRule")
                .executeTime(LocalDateTime.now())
                .executeDuration(100L)
                .success(success)
                .build();
    }
} 