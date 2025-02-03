package com.demo.thread.badcase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsePoolGoodCase {

    public static void main(String[] args) {
        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        virtualExecutor.submit(() -> {
            // 在虚拟线程中使用平台线程池
            doSomeWork();
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
