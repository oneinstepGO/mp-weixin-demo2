package com.demo.thread.badcase;

import java.util.concurrent.CompletableFuture;

public class PinnedGoodCase {

    public class NonPinnedExample {
        public void doWork() {
            Thread.ofVirtual().start(() -> {
                // 使用非阻塞操作或轻量级同步机制
                CompletableFuture.runAsync(this::heavyComputation);
            });
        }

        private void heavyComputation() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
