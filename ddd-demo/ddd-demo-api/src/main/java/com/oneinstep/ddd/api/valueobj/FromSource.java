package com.oneinstep.ddd.api.valueobj;

import java.io.Serial;
import java.io.Serializable;

/**
 * 业务来源三元组-用于幂等
 *
 * @param fromSourceType  来源类型-用于幂等
 * @param fromSourceId    来源ID-用于幂等
 * @param fromSourceSubId 来源子ID-用于幂等
 */
public record FromSource(Integer fromSourceType, Long fromSourceId, Long fromSourceSubId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static FromSource of(Integer fromSourceType, Long fromSourceId, Long fromSourceSubId) {
        return new FromSource(fromSourceType, fromSourceId, fromSourceSubId);
    }

    public String toKey() {
        return String.format("%d-%d-%d", fromSourceType, fromSourceId, fromSourceSubId);
    }
}
