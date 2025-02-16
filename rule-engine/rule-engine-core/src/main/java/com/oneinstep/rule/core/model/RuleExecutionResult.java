package com.oneinstep.rule.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {

    private boolean success;  // 执行是否成功
    private String errorMessage;  // 错误信息
    private int rulesExecuted;  // 执行的规则数量

    @Builder.Default
    private List<Object> facts = new ArrayList<>();  // 执行后的事实对象

    /**
     * 获取指定类型的事实对象
     */
    public <T> T getFact(Class<T> clazz) {
        return facts.stream()
                .filter(f -> clazz.isInstance(f))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指定类型的所有事实对象
     */
    public <T> List<T> getFacts(Class<T> clazz) {
        return facts.stream()
                .filter(f -> clazz.isInstance(f))
                .map(clazz::cast)
                .toList();
    }
} 