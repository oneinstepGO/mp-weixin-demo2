package com.demo.thread.badcase;

import java.util.concurrent.Executors;

public class ThreadLocalBadCase {

    private static ThreadLocal<User> userContext = new ThreadLocal<>();

    public void processUser(User user) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                userContext.set(user); // 在虚拟线程中使用 ThreadLocal
                doSomeWork();
                userContext.remove();
            });
        }
    }

    private void doSomeWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
