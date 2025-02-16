package com.oneinstep.rule.core.service;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.rule.core.config.RuleLogProperties;
import com.oneinstep.rule.core.model.RuleExecutionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 规则执行日志服务
 */
@Slf4j
@Service
public class RuleExecutionLogService {
    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 日志队列
    private final LinkedBlockingQueue<RuleExecutionLog> logQueue;
    // 规则执行统计
    private final Map<String, Long> ruleExecutionStats;
    // 日志文件路径
    private final String logFilePath;

    public RuleExecutionLogService(RuleLogProperties logProperties) {
        this.logQueue = new LinkedBlockingQueue<>(10000);
        this.ruleExecutionStats = new ConcurrentHashMap<>();

        // 确保日志目录存在
        File logDir = new File(logProperties.getLogDir());
        if (!logDir.exists() && !logDir.mkdirs()) {
            log.error("Failed to create log directory: {}", logProperties.getLogDir());
        }

        this.logFilePath = new File(logDir, logProperties.getExecutionLogFile()).getPath();
        log.info("Rule execution log file path: {}", this.logFilePath);
    }

    /**
     * 记录规则执行日志
     *
     * @param executionLog 规则执行日志
     */
    public void logExecution(RuleExecutionLog executionLog) {
        try {
            if (!logQueue.offer(executionLog)) {
                log.warn("Log queue is full, execution log for rule {} was dropped",
                        executionLog.getRuleId());
            }

            String key = executionLog.getRuleGroup();
            if (key != null) {
                updateStats(key, executionLog.getExecuteDuration());
            }

            // 写入文件日志
            writeToFile(executionLog);

        } catch (Exception e) {
            log.error("Failed to log rule execution", e);
        }
    }

    /**
     * 写入文件日志
     *
     * @param executionLog 规则执行日志
     */
    private void writeToFile(RuleExecutionLog executionLog) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            String logEntry = String.format("[%s] Group: %s, RuleId: %s, Duration: %dms, Success: %s%s, Details: %s",
                    DATE_FORMATTER.format(executionLog.getExecuteTime()),
                    executionLog.getRuleGroup(),
                    executionLog.getRuleId(),
                    executionLog.getExecuteDuration(),
                    executionLog.isSuccess(),
                    executionLog.isSuccess() ? "" : ", Error: " + executionLog.getErrorMessage(),
                    JSON.toJSONString(executionLog));
            writer.println(logEntry);
        } catch (IOException e) {
            log.error("Failed to write to log file", e);
        }
    }

    /**
     * 更新规则执行统计
     *
     * @param key      规则组
     * @param duration 执行时间
     */
    private void updateStats(String key, Long duration) {
        if (duration != null) {
            ruleExecutionStats.compute(key, (k, v) -> {
                if (v == null) {
                    return duration;
                }
                return (v + duration) / 2; // 计算平均执行时间
            });
        }
    }

    /**
     * 获取规则组平均执行时间
     *
     * @param group 规则组
     * @return 平均执行时间
     */
    public Long getAverageExecutionTime(String group) {
        return ruleExecutionStats.get(group);
    }
} 