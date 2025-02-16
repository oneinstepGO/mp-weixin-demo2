package com.oneinstep.rule.core.model;

import lombok.Builder;
import lombok.Data;
import org.kie.api.runtime.rule.AgendaFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class RuleExecutionContext {

    private String ruleGroup;  // 规则组

    @Builder.Default
    private List<Object> facts = new ArrayList<>();  // 事实对象列表

    @Builder.Default
    private Map<String, Object> globals = new HashMap<>();  // 全局变量

    private AgendaFilter agendaFilter;  // 规则过滤器

    /**
     * 添加事实对象
     */
    public void addFact(Object fact) {
        facts.add(fact);
    }

    /**
     * 添加全局变量
     */
    public void setGlobal(String name, Object value) {
        globals.put(name, value);
    }
} 