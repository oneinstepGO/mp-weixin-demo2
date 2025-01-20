package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaMoneyBalanceLogRepository extends JpaRepository<MoneyBalanceLog, Long> {

    /**
     * 根据来源类型、来源ID、来源子ID查询资金变动记录
     *
     * @param fromSourceType  来源类型
     * @param fromSourceId    来源ID
     * @param fromSourceSubId 来源子ID
     * @return 资金变动记录
     */
    MoneyBalanceLog findMoneyBalanceLogByFromSourceTypeAndFromSourceIdAndFromSourceSubId(
            Integer fromSourceType,
            Long fromSourceId,
            Long fromSourceSubId);

    /**
     * 根据来源类型、来源ID查询资金变动记录
     *
     * @param fromSourceType 来源类型
     * @param fromSourceId   来源ID
     * @return 资金变动记录
     */
    List<MoneyBalanceLog> findMoneyBalanceLogsByFromSourceTypeAndFromSourceId(Integer fromSourceType, Long fromSourceId);

}
