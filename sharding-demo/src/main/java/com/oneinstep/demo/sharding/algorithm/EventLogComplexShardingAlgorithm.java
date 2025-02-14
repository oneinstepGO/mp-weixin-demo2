package com.oneinstep.demo.sharding.algorithm;

import com.oneinstep.demo.sharding.generator.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 事件日志复合分片算法
 * 支持按ID和时间进行分片路由，实现按月分表
 */
@Slf4j
public class EventLogComplexShardingAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {

    // 表后缀日期格式：yyyyMM
    private static final String SUFFIX_PATTERN = "yyyyMM";
    // 创建时间字段名
    private static final String CREATE_TIME_COLUMN = "create_time";
    // ID字段名
    private static final String ID_COLUMN = "id";

    /**
     * 检查时间是否在有效范围内
     * 根据实际表名推断有效时间范围
     */
    private boolean isTimeInRange(LocalDateTime dateTime, Collection<String> availableTargetNames) {
        if (availableTargetNames.isEmpty()) {
            return false;
        }

        // 从可用表名中提取最小和最大年月
        int minYearMonth = Integer.MAX_VALUE;
        int maxYearMonth = Integer.MIN_VALUE;

        for (String tableName : availableTargetNames) {
            String yearMonth = tableName.substring(tableName.length() - 6); // 提取后6位年月
            int yearMonthInt = Integer.parseInt(yearMonth);
            minYearMonth = Math.min(minYearMonth, yearMonthInt);
            maxYearMonth = Math.max(maxYearMonth, yearMonthInt);
        }

        // 将日期转换为年月格式
        int dateYearMonth = Integer.parseInt(dateTime.format(DateTimeFormatter.ofPattern(SUFFIX_PATTERN)));

        return dateYearMonth >= minYearMonth && dateYearMonth <= maxYearMonth;
    }

    /**
     * 执行分片路由
     *
     * @param availableTargetNames 可用的目标表名集合，如: [event_log_202501, event_log_202502, ...]
     * @param shardingValue        分片值，包含分片键和对应的值
     * @return 路由后的目标表名集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Comparable<?>> shardingValue) {
        Collection<String> result = new LinkedHashSet<>();
        Map<String, Collection<Comparable<?>>> columnMap = shardingValue.getColumnNameAndShardingValuesMap();

        // 1. 如果有 ID，《优先》使用 ID 中的时间戳信息路由
        // 因为ID中包含了生成时间信息，可以直接定位到具体的分片表
        if (columnMap.containsKey(ID_COLUMN)) {
            Collection<Comparable<?>> ids = columnMap.get(ID_COLUMN);
            for (Comparable<?> idObj : ids) {
                long id = ((Number) idObj).longValue();
                // 从雪花算法ID中提取年月信息
                String yearMonth = SnowflakeIdGenerator.extractYearMonth(id);
                log.debug("Routing by ID: {}, extracted yearMonth: {}", id, yearMonth);
                // 构造目标表名：逻辑表名_年月
                result.add(shardingValue.getLogicTableName() + "_" + yearMonth);
            }
            return result;
        }

        // 2. 处理 create_time 路由
        if (columnMap.containsKey(CREATE_TIME_COLUMN)) {
            Collection<Comparable<?>> createTimes = columnMap.get(CREATE_TIME_COLUMN);
            for (Comparable<?> time : createTimes) {
                LocalDateTime dateTime = (LocalDateTime) time;
                // 检查时间是否在有效范围内
                if (!isTimeInRange(dateTime, availableTargetNames)) {
                    log.warn("Create time {} is out of available table range", dateTime);
                    continue;
                }
                String suffix = dateTime.format(DateTimeFormatter.ofPattern(SUFFIX_PATTERN));
                result.add(shardingValue.getLogicTableName() + "_" + suffix);
            }
            return result.isEmpty() ? availableTargetNames : result;
        }

        // 3. 处理范围查询
        Map<String, com.google.common.collect.Range<Comparable<?>>> rangeMap = shardingValue.getColumnNameAndRangeValuesMap();
        if (rangeMap.containsKey(CREATE_TIME_COLUMN)) {
            com.google.common.collect.Range<Comparable<?>> range = rangeMap.get(CREATE_TIME_COLUMN);
            LocalDateTime start = (LocalDateTime) range.lowerEndpoint();
            LocalDateTime end = (LocalDateTime) range.upperEndpoint();

            // 遍历范围内的每个月份
            LocalDateTime current = start;
            while (!current.isAfter(end)) {
                if (isTimeInRange(current, availableTargetNames)) {
                    String suffix = current.format(DateTimeFormatter.ofPattern(SUFFIX_PATTERN));
                    result.add(shardingValue.getLogicTableName() + "_" + suffix);
                }
                current = current.plusMonths(1).withDayOfMonth(1);
            }
            return result.isEmpty() ? availableTargetNames : result;
        }

        // 4. 如果没有分片条件，返回所有可用表
        // 这种情况应该避免，因为会导致全表扫描
        return availableTargetNames;
    }

    @Override
    public String getType() {
        // 使用基于类的分片策略
        return "CLASS_BASED";
    }
}