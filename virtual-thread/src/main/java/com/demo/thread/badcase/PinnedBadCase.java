package com.demo.thread.badcase;

public class PinnedBadCase {

    public class PinnedExample {
        public void doWork() {
            Thread.ofVirtual().start(() -> {
                // 这些操作会导致虚拟线程被固定到载体线程
                synchronized (this) {
                    heavyComputation();
                }
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
