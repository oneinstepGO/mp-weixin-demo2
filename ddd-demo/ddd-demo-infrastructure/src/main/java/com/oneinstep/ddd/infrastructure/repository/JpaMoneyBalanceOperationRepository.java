package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaMoneyBalanceOperationRepository extends JpaRepository<MoneyBalanceOperation, Long> {

    /**
     * 根据来源类型、来源ID、来源子ID查询资金变动记录
     *
     * @param fromSourceType  来源类型
     * @param fromSourceId    来源ID
     * @param fromSourceSubId 来源子ID
     * @return 资金变动记录
     */
    MoneyBalanceOperation findMoneyBalanceOperationByFromSourceTypeAndFromSourceIdAndFromSourceSubId(
            Integer fromSourceType,
            Long fromSourceId,
            Long fromSourceSubId);

}
