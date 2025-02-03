package com.demo.thread.badcase;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedGoodCase {

    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                counter.incrementAndGet();
            });
        }
    }
}
