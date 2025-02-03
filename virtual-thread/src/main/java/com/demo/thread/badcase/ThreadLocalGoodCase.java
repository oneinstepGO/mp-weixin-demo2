package com.demo.thread.badcase;

import java.util.concurrent.Executors;

public class ThreadLocalGoodCase {

    // 创建一个ScopedValue实例来存储User上下文
    private static final ScopedValue<User> userContext = ScopedValue.newInstance();

    public void processUser(User user) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                // 使用where()方法绑定值,并在run()中执行业务逻辑
                ScopedValue.where(userContext, user)
                        .run(() -> {
                            // 在这里可以通过get()方法获取上下文中的user对象
                            User currentUser = userContext.get();
                            System.out.println("Current user: " + currentUser);
                            doSomeWork();
                        });
            });
        }
    }

    private void doSomeWork() {
        try {
            // 在方法中也可以访问上下文中的user
            User user = userContext.get();
            System.out.println("Processing user: " + user);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
