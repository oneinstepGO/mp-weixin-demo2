package com.oneinstep.ddd.common.util.publisher;

/**
 * 领域事件发布器
 */
public interface DomainEventPublisher {

    /**
     * 发布事件
     *
     * @param event 事件
     */
    void publish(Object event);
}
