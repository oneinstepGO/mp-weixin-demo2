package com.demo.thread.badcase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsePoolBadCase {

    public static void main(String[] args) {
        ExecutorService platformExecutor = Executors.newFixedThreadPool(100);
        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        virtualExecutor.submit(() -> {
            // 在虚拟线程中使用平台线程池
            platformExecutor.submit(() -> {
                // 这里的任务会被阻塞在有限的平台线程池中
                doSomeWork();
            });
        });
    }

    private static void doSomeWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
