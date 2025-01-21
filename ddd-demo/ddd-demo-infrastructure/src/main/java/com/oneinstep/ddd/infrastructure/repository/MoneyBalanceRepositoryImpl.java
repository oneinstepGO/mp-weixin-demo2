package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceRepository;
import com.oneinstep.ddd.domain.money.valueobj.MoneyBalanceChg;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class MoneyBalanceRepositoryImpl implements IMoneyBalanceRepository {

    @Resource
    private JpaMoneyBalanceRepository jpaMoneyBalanceRepository;

    @Override
    public List<MoneyBalance> findByAssetAccountId(Long assetAccountId) {
        return jpaMoneyBalanceRepository.findMoneyBalancesByAssetAccountId(assetAccountId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncrementalUpdate(List<MoneyBalanceChg> moneyBalanceChgs) {
        if (moneyBalanceChgs == null || moneyBalanceChgs.isEmpty()) {
            return false;
        }
        int rows = 0;

        for (MoneyBalanceChg moneyBalanceChg : moneyBalanceChgs) {
            int incrementalUpdate = jpaMoneyBalanceRepository.incrementalUpdate(moneyBalanceChg);
            if (incrementalUpdate > 0) {
                rows++;
            }
        }

        return rows == moneyBalanceChgs.size();
    }

    @Override
    public MoneyBalance save(MoneyBalance moneyBalance) {
        return jpaMoneyBalanceRepository.save(moneyBalance);
    }

    @Override
    public boolean saveBatch(List<MoneyBalance> moneyBalances) {
        List<MoneyBalance> saveAll = jpaMoneyBalanceRepository.saveAll(moneyBalances);
        return saveAll.size() == moneyBalances.size();
    }

    @Override
    public void deleteAll() {
        jpaMoneyBalanceRepository.deleteAll();
    }

}
