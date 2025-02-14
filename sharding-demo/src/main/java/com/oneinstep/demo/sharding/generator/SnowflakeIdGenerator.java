package com.oneinstep.demo.sharding.generator;

import com.mybatisflex.core.keygen.IKeyGenerator;
import lombok.extern.slf4j.Slf4j;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 标准雪花算法ID生成器
 * <p>
 * ID结构：
 * |-- 符号位(1) --|-- 时间戳(41) --|-- 机器ID(10) --|-- 序列号(12) --|
 * <p>
 * 各部分说明：
 * - 符号位：始终为0，保证ID为正数
 * - 时间戳：41位，精确到毫秒，可以使用69年
 * - 机器ID：10位，最多支持1024个节点
 * - 序列号：12位，同一毫秒内最多生成4096个ID
 * 支持最大并发为1024*4096 = 4194304/s，这个值已经足够大了，可以满足大部分场景
 * <p>
 * 优化特性：
 * 1. 支持自动生成workerId，多种策略保证唯一性
 * 2. 内置时钟回拨处理机制
 * 3. 支持指定时间生成ID，方便测试和特殊场景
 */
@Slf4j
public class SnowflakeIdGenerator implements IKeyGenerator {

    // ====== 基础配置常量 ======
    /**
     * 基准时间戳 (2022-01-01 00:00:00)
     * 用于计算相对时间戳，避免时间戳位数过大
     */
    private static final long EPOCH = 1640995200000L;

    /**
     * 机器ID位数 (10位)
     * 可以部署在1024个节点上
     */
    private static final long WORKER_ID_BITS = 10L;

    /**
     * 序列号位数 (12位)
     * 每毫秒可以生成4096个ID
     */
    private static final long SEQUENCE_BITS = 12L;

    // ====== 位运算相关常量 ======
    /**
     * 最大机器ID = 1023 (2^10 - 1)
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 最大序列号 = 4095 (2^12 - 1)
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数 (12)
     */
    private static final long WORKER_SHIFT = SEQUENCE_BITS;

    /**
     * 时间戳左移位数 (22)
     */
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    // ====== 时钟回拨处理相关配置 ======
    /**
     * 最大容忍的时钟回拨毫秒数
     */
    private static final long MAX_BACKWARD_MS = 10L;

    /**
     * 时钟回拨时的自旋次数
     */
    private static final long CLOCK_BACK_SPIN_COUNT = 10L;

    // ====== 运行时状态 ======
    /**
     * 当前机器ID
     */
    private final long workerId;

    /**
     * 序列号，默认从0开始
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 初始化状态标记
     */
    private boolean initialized = false;

    /**
     * 构造函数
     * 初始化机器ID并记录日志
     */
    public SnowflakeIdGenerator() {
        this.workerId = initWorkerId();
        this.initialized = true;
        log.info("SnowflakeIdGenerator initialized with workerId: {}", workerId);
    }

    /**
     * 生成下一个ID (MyBatis-Flex接口实现)
     *
     * @param entity 实体对象
     * @param column 列名
     * @return 生成的ID
     */
    @Override
    public synchronized Object generate(Object entity, String column) {
        if (!initialized) {
            throw new IllegalStateException("SnowflakeIdGenerator not initialized");
        }

        try {
            long currentTimestamp = getCurrentTimestamp();

            // 处理时钟回拨
            if (currentTimestamp < lastTimestamp) {
                long offset = lastTimestamp - currentTimestamp;
                if (offset <= MAX_BACKWARD_MS) {
                    spinWaitForClockBack(offset);
                    currentTimestamp = getCurrentTimestamp();
                } else {
                    String message = String.format("Clock moved backwards by %d ms", offset);
                    log.error(message);
                    throw new RuntimeException(message);
                }
            }

            // 同一毫秒内序列号自增
            if (currentTimestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                // 当前毫秒序列号用完，等待下一毫秒
                if (sequence == 0) {
                    currentTimestamp = waitForNextMillis(lastTimestamp);
                }
            } else {
                // 不同毫秒，序列号从0开始
                sequence = ThreadLocalRandom.current().nextLong(0, 2);
            }

            lastTimestamp = currentTimestamp;

            // 组装ID：时间戳 + 机器ID + 序列号
            return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                    | (workerId << WORKER_SHIFT)
                    | sequence;

        } catch (Exception e) {
            log.error("Failed to generate ID", e);
            throw e;
        }
    }

    /**
     * 根据指定时间生成ID
     * 用于测试或特殊场景
     *
     * @param dateTime 指定的时间
     * @return 生成的ID
     */
    public synchronized long generateWithTime(LocalDateTime dateTime) {
        if (!initialized) {
            throw new IllegalStateException("SnowflakeIdGenerator not initialized");
        }

        try {
            // 转换为毫秒时间戳
            long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // 使用随机序列号
            sequence = ThreadLocalRandom.current().nextLong(0, 2);

            // 组装ID：时间戳 + 机器ID + 序列号
            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                    | (workerId << WORKER_SHIFT)
                    | sequence;

        } catch (Exception e) {
            log.error("Failed to generate ID with time: {}", dateTime, e);
            throw e;
        }
    }

    /**
     * 从ID中提取时间戳信息
     *
     * @param id 雪花算法生成的ID
     * @return 时间戳对应的LocalDateTime
     */
    public static LocalDateTime extractDateTime(long id) {
        // 提取时间戳部分并还原
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 从ID中提取年月信息
     *
     * @param id 雪花算法生成的ID
     * @return 年月字符串，格式：yyyyMM
     */
    public static String extractYearMonth(long id) {
        LocalDateTime dateTime = extractDateTime(id);
        return String.format("%d%02d", dateTime.getYear(), dateTime.getMonthValue());
    }

    // ====== 内部工具方法 ======

    /**
     * 初始化workerId
     * 支持多种策略：环境变量 -> MAC地址 -> 随机数
     */
    private long initWorkerId() {
        // 1. 优先使用环境变量
        String workerIdStr = System.getenv("WORKER_ID");
        if (workerIdStr != null) {
            try {
                long id = Long.parseLong(workerIdStr);
                if (id <= MAX_WORKER_ID) {
                    return id;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid WORKER_ID environment variable", e);
            }
        }

        // 2. 尝试使用MAC地址最后几位
        try {
            long id = generateWorkerIdFromMac();
            if (id >= 0) {
                return id;
            }
        } catch (Exception e) {
            log.warn("Failed to generate workerId from MAC address", e);
        }

        // 3. 降级使用随机数
        long randomId = ThreadLocalRandom.current().nextLong(MAX_WORKER_ID);
        log.warn("Using random workerId: {}", randomId);
        return randomId;
    }

    /**
     * 基于MAC地址生成workerId
     */
    private long generateWorkerIdFromMac() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();
            byte[] mac = network.getHardwareAddress();
            if (mac != null && mac.length > 0) {
                return ((0x000000FF & (long) mac[mac.length - 1]) |
                        (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) % (MAX_WORKER_ID + 1);
            }
        }
        return -1;
    }

    /**
     * 获取当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     */
    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 处理时钟回拨的自旋等待
     */
    private void spinWaitForClockBack(long offset) {
        long totalWaitTime = 0;
        long spinCount = 0;
        long sleepTime = 1L;

        while (spinCount < CLOCK_BACK_SPIN_COUNT && totalWaitTime < offset) {
            try {
                Thread.sleep(sleepTime);
                totalWaitTime += sleepTime;
                spinCount++;
                sleepTime *= 2; // 指数退避
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}