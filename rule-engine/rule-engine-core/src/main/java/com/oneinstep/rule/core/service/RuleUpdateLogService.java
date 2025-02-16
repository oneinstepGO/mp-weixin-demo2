package com.oneinstep.rule.core.service;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.rule.core.config.RuleLogProperties;
import com.oneinstep.rule.core.model.RuleUpdateLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 规则更新日志服务
 */
@Slf4j
@Service
public class RuleUpdateLogService {

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 更新日志队列
    private final LinkedBlockingQueue<RuleUpdateLog> updateLogs;
    // 最近更新日志列表
    private final List<RuleUpdateLog> recentLogs;
    // 日志文件路径
    private final String logFilePath;
    // 最大最近更新日志数量
    private static final int MAX_RECENT_LOGS = 100;

    public RuleUpdateLogService(RuleLogProperties logProperties) {
        this.updateLogs = new LinkedBlockingQueue<>(1000);
        this.recentLogs = new ArrayList<>();

        // 确保日志目录存在
        File logDir = new File(logProperties.getLogDir());
        if (!logDir.exists() && !logDir.mkdirs()) {
            log.error("Failed to create log directory: {}", logProperties.getLogDir());
        }

        this.logFilePath = new File(logDir, logProperties.getUpdateLogFile()).getPath();
        log.info("Rule update log file path: {}", this.logFilePath);
    }

    /**
     * 记录更新日志
     */
    public void logUpdate(RuleUpdateLog updateLog) {
        try {
            // 如果日志队列满了，则丢弃日志
            if (!updateLogs.offer(updateLog)) {
                log.warn("Update log queue is full, log was dropped for group: {}",
                        updateLog.getRuleGroup());
            }
            // 同步更新最近更新日志列表
            synchronized (recentLogs) {
                // 添加到最近更新日志列表
                recentLogs.addFirst(updateLog);
                // 如果最近更新日志列表超过了最大数量，则移除最后一个
                if (recentLogs.size() > MAX_RECENT_LOGS) {
                    recentLogs.removeLast();
                }
            }

            // 写入文件日志
            writeToFile(updateLog);

        } catch (Exception e) {
            log.error("Failed to log rule update", e);
        }
    }

    /**
     * 写入文件日志
     */
    private void writeToFile(RuleUpdateLog updateLog) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            // 写入日志
            String logEntry = String.format("[%s] Group: %s, Rules: %s, Operator: %s, Success: %s%s",
                    DATE_FORMATTER.format(updateLog.getUpdateTime()),
                    updateLog.getRuleGroup(),
                    JSON.toJSONString(updateLog.getUpdatedRuleIds()),
                    updateLog.getOperator(),
                    updateLog.isSuccess(),
                    updateLog.isSuccess() ? "" : ", Error: " + updateLog.getErrorMessage());
            writer.println(logEntry);
        } catch (IOException e) {
            log.error("Failed to write to log file", e);
        }
    }

    /**
     * 获取最近更新日志
     */
    public List<RuleUpdateLog> getRecentLogs() {
        synchronized (recentLogs) {
            return new ArrayList<>(recentLogs);
        }
    }

    /**
     * 获取日志文件路径
     */
    public String getLogFilePath() {
        return logFilePath;
    }
}