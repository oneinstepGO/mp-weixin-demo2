package com.oneinstep.rule.demo.model.trade;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class RiskCheckResult {
    @Builder.Default
    private List<RiskViolation> violations = new ArrayList<>();
    private boolean passed;

    public void addViolation(String rule, String message) {
        violations.add(RiskViolation.builder()
                .rule(rule)
                .message(message)
                .build());
        passed = false;
    }

    @Data
    @Builder
    static
    class RiskViolation {
        private String rule;
        private String message;
    }
}

