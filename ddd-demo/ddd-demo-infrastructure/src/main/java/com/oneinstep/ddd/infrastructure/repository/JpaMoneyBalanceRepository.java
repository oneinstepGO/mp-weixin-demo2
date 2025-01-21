package com.oneinstep.ddd.infrastructure.repository;

import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.valueobj.MoneyBalanceChg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金余额仓储
 */
@Repository
public interface JpaMoneyBalanceRepository extends JpaRepository<MoneyBalance, Long> {

    /**
     * 根据钱包ID查询钱包
     *
     * @param assetAccountId 资金帐户id
     * @return 所有币种的钱包
     */
    List<MoneyBalance> findMoneyBalancesByAssetAccountId(Long assetAccountId);

    /**
     * 增量更新单个钱包
     *
     * @param chg 钱包变动对象
     * @return 是否更新成功
     */
    @Modifying
    @Query(value = "UPDATE money_balance SET " +
            "current_balance = current_balance + :#{#chg.chgCurrentBalance}, " +
            "frozen_amount = frozen_amount + :#{#chg.chgFrozenAmount}, " +
            "version = version + 1 " +
            "WHERE id = :#{#chg.id} AND version = :#{#chg.version}", nativeQuery = true)
    int incrementalUpdate(@Param("chg") MoneyBalanceChg chg);

}