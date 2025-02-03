package com.demo.thread;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadDemo {

    public static void main(String[] args) {
        // 1. 创建并等待虚拟线程完成
        try {
            Thread.ofVirtual()
                    .name("demo-virtual-thread")
                    .start(() -> {
                        System.out.println("Running in: " + Thread.currentThread());
                        try {
                            Thread.sleep(Duration.ofSeconds(1));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).join();  // 等待线程完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. 使用虚拟线程执行器
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 提交1000个任务
            IntStream.range(0, 1000).forEach(i -> {
                executor.submit(() -> {
                    // 模拟IO操作
                    Thread.sleep(Duration.ofMillis(100));
                    System.out.printf("Task %d completed%n", i);
                    return i;
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 对比测试
        performanceComparison();
    }

    private static void performanceComparison() {
        // 使用传统线程池
        long start = System.currentTimeMillis();
        try (var executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(100));
                    return "Done";
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Platform threads took: " +
                (System.currentTimeMillis() - start) + "ms");

        // 使用虚拟线程
        start = System.currentTimeMillis();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(100));
                    return "Done";
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Virtual threads took: " +
                (System.currentTimeMillis() - start) + "ms");
    }
} 