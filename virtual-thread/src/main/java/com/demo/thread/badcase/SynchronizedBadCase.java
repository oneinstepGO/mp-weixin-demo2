package com.demo.thread.badcase;

import java.util.concurrent.Executors;

public class SynchronizedBadCase {

    private final Object lock = new Object();
    private int counter = 0;

    public void increment() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                synchronized (lock) {  // 使用同步块会阻塞载体线程
                    counter++;
                }
            });
        }
    }
}
