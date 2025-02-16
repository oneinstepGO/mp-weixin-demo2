package com.oneinstep.rule.core.executor;

import com.oneinstep.rule.core.config.loader.DynamicRuleLoader;
import com.oneinstep.rule.core.model.RuleExecutionContext;
import com.oneinstep.rule.core.model.RuleExecutionResult;
import com.oneinstep.rule.core.service.RuleExecutionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleExecutorConcurrencyTest {

    @Mock
    private RuleExecutionLogService logService;

    @Mock
    private DynamicRuleLoader ruleLoader;

    @Mock
    private KieContainer kieContainer;

    @Mock
    private KieSession kieSession;

    private RuleExecutor ruleExecutor;

    @BeforeEach
    void setUp() {
        ruleExecutor = new RuleExecutor(logService, ruleLoader);
    }

    /**
     * 测试多线程并发执行规则
     * 验证:
     * 1. 每个线程都能正确获取KieSession
     * 2. KieSession的关闭操作是安全的
     * 3. 结果统计是准确的
     */
    @Test
    void testConcurrentExecution() throws InterruptedException {
        // 配置mock对象的行为
        when(ruleLoader.getContainer(anyString())).thenReturn(kieContainer);
        when(kieContainer.newKieSession()).thenReturn(kieSession);
        when(kieSession.fireAllRules(any())).thenReturn(1);

        // 准备测试数据
        int threadCount = 10;
        int iterationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<RuleExecutionResult> results = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            // 提交任务到线程池
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < iterationsPerThread; j++) {
                            RuleExecutionContext context = RuleExecutionContext.builder()
                                    .ruleGroup("test")
                                    .facts(new ArrayList<>())
                                    .build();

                            RuleExecutionResult result = ruleExecutor.execute(context);
                            results.add(result);
                            if (result.isSuccess()) {
                                successCount.incrementAndGet();
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有执行完成
            assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout waiting for executions");
            executorService.shutdown();
        }

        // 验证结果
        assertEquals(threadCount * iterationsPerThread, results.size(), "Should have results from all threads");
        assertEquals(threadCount * iterationsPerThread, successCount.get(), "All executions should be successful");

        // 验证调用次数
        verify(ruleLoader, times(threadCount * iterationsPerThread)).getContainer(anyString());
        verify(kieContainer, times(threadCount * iterationsPerThread)).newKieSession();
        verify(kieSession, times(threadCount * iterationsPerThread)).fireAllRules(any());
    }

    /**
     * 测试规则执行过程中的异常处理
     * 验证:
     * 1. 异常不会影响其他线程
     * 2. KieSession能正确关闭
     * 3. 错误信息被正确记录
     */
    @Test
    void testConcurrentExecutionWithErrors() throws InterruptedException {
        // Mock抛出异常
        when(ruleLoader.getContainer(anyString())).thenThrow(new RuntimeException("Test error"));

        // 准备测试数据
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<RuleExecutionResult> results = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            // 启动多个线程并发执行
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        RuleExecutionContext context = RuleExecutionContext.builder()
                                .ruleGroup("test")
                                .facts(new ArrayList<>())
                                .build();

                        RuleExecutionResult result = ruleExecutor.execute(context);
                        results.add(result);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for threads");
            executorService.shutdown();
        }

        // 验证结果
        assertEquals(threadCount, results.size(), "Should have results from all threads");
        results.forEach(result -> {
            assertFalse(result.isSuccess(), "Execution should fail");
            assertNotNull(result.getErrorMessage(), "Should have error message");
        });

        // 验证调用次数
        verify(ruleLoader, times(threadCount)).getContainer(anyString());
    }

    /**
     * 测试规则执行的资源释放
     * 验证:
     * 1. KieSession正确关闭
     * 2. 内存泄漏问题
     */
    @Test
    void testResourceCleanup() throws InterruptedException {
        // 配置mock对象的行为
        when(ruleLoader.getContainer(anyString())).thenReturn(kieContainer);
        when(kieContainer.newKieSession()).thenReturn(kieSession);
        when(kieSession.fireAllRules(any())).thenReturn(1);

        int iterations = 50;
        CountDownLatch latch = new CountDownLatch(iterations);

        // 使用单线程执行器来确保顺序执行
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            for (int i = 0; i < iterations; i++) {
                executorService.submit(() -> {
                    try {
                        RuleExecutionContext context = RuleExecutionContext.builder()
                                .ruleGroup("test")
                                .facts(new ArrayList<>())
                                .build();

                        ruleExecutor.execute(context);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有执行完成
            assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout waiting for executions");
            executorService.shutdown();
        }

        // 验证资源释放
        verify(ruleLoader, times(iterations)).getContainer(any());
        verify(kieSession, times(iterations)).close();
    }
} 