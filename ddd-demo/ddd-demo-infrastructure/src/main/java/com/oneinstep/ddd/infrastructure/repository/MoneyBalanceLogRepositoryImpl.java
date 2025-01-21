package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceLogRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金余额日志仓储
 */
@Repository
public class MoneyBalanceLogRepositoryImpl implements IMoneyBalanceLogRepository {

    @Resource
    private JpaMoneyBalanceLogRepository jpaMoneyBalanceLogRepository;

    @Override
    public List<MoneyBalanceLog> findLogsBy2F(Integer fromSourceType, Long fromSourceId) {
        return jpaMoneyBalanceLogRepository.findMoneyBalanceLogsByFromSourceTypeAndFromSourceId(fromSourceType, fromSourceId);
    }

    @Override
    public MoneyBalanceLog findBy3F(FromSource fromSource) {
        return jpaMoneyBalanceLogRepository.findMoneyBalanceLogByFromSourceTypeAndFromSourceIdAndFromSourceSubId(
                fromSource.fromSourceType(), fromSource.fromSourceId(), fromSource.fromSourceSubId());
    }

    @Override
    public boolean saveLog(MoneyBalanceLog moneyBalanceLog) {
        return jpaMoneyBalanceLogRepository.save(moneyBalanceLog) != null;
    }

    @Override
    public boolean saveLogBatch(List<MoneyBalanceLog> moneyBalanceLogs) {
        return jpaMoneyBalanceLogRepository.saveAll(moneyBalanceLogs) != null;
    }

    @Override
    public void deleteAll() {
        jpaMoneyBalanceLogRepository.deleteAll();
    }
}
