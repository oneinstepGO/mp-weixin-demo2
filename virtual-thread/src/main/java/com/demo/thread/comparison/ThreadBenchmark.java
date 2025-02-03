package com.demo.thread.comparison;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
public class ThreadBenchmark {

    @Param({"100", "1000", "10000"})
    private int taskCount;

    @Param({"10", "100"})
    private int blockingMs;

    @Benchmark
    public void platformThreadTest() {
        try (var executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    simulateBlockingOperation();
                    return "Done";
                });
            }
        }
    }

    @Benchmark
    public void virtualThreadTest() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    simulateBlockingOperation();
                    return "Done";
                });
            }
        }
    }

    private void simulateBlockingOperation() {
        try {
            // 模拟IO阻塞操作
            Thread.sleep(Duration.ofMillis(blockingMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws RunnerException {
        // 添加系统属性
        System.setProperty("jmh.ignoreLock", "true");

        Options opt = new OptionsBuilder()
                .include(ThreadBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(2)
                .measurementIterations(3)
                .threads(1)
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        new Runner(opt).run();
    }
} 