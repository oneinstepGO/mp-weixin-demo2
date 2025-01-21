package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceOperationRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * 资金余额操作仓储
 */
@Repository
public class MoneyBalanceOperationRepositoryImpl implements IMoneyBalanceOperationRepository {

    @Resource
    private JpaMoneyBalanceOperationRepository jpaMoneyBalanceOperationRepository;

    @Override
    public MoneyBalanceOperation findBy3F(FromSource fromSource) {
        return jpaMoneyBalanceOperationRepository
                .findMoneyBalanceOperationByFromSourceTypeAndFromSourceIdAndFromSourceSubId(
                        fromSource.fromSourceType(), fromSource.fromSourceId(), fromSource.fromSourceSubId());
    }

    @Override
    public MoneyBalanceOperation save(MoneyBalanceOperation moneyBalanceOperation) {
        return jpaMoneyBalanceOperationRepository.save(moneyBalanceOperation);
    }

    @Override
    public void deleteAll() {
        jpaMoneyBalanceOperationRepository.deleteAll();
    }
}
