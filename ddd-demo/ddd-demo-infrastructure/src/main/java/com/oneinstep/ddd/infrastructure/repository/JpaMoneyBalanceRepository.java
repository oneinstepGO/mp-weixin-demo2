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
     * @param moneyBalanceChg 钱包变动对象
     * @return 是否更新成功
     */
    @Modifying
    @Query(value = "UPDATE money_balance SET " +
            "current_balance = current_balance + :#{#chg.chgCurrentBalance}, " +
            "frozen_amount = frozen_amount + :#{#chg.chgFrozenAmount}, " +
            "money_in_today = money_in_today + :#{#chg.chgMoneyInToday}, " +
            "money_out_today = money_out_today + :#{#chg.chgMoneyOutToday}, " +
            "version = version + 1 " +
            "WHERE id = :#{#chg.id} AND version = :#{#chg.version}", nativeQuery = true)
    int incrementalUpdateMoneyBalance(@Param("chg") MoneyBalanceChg chg);

    /**
     * 批量增量更新钱包
     *
     * @param chgs 钱包变动对象列表
     * @return 更新成功的记录数
     */
    @Modifying
    @Query(value = "UPDATE money_balance mb " +
            "SET current_balance = current_balance + " +
            "CASE mb.id " +
            "    #{#chgs.![WHEN id THEN chgCurrentBalance]} " +
            "END, " +
            "frozen_amount = frozen_amount + " +
            "CASE mb.id " +
            "    #{#chgs.![WHEN id THEN chgFrozenAmount]} " +
            "END, " +
            "money_in_today = money_in_today + " +
            "CASE mb.id " +
            "    #{#chgs.![WHEN id THEN chgMoneyInToday]} " +
            "END, " +
            "money_out_today = money_out_today + " +
            "CASE mb.id " +
            "    #{#chgs.![WHEN id THEN chgMoneyOutToday]} " +
            "END, " +
            "version = version + 1 " +
            "WHERE mb.id IN (:#{#chgs.![id]}) " +
            "AND mb.version IN (:#{#chgs.![version]})", nativeQuery = true)
    int incrementalUpdateMoneyBalances(@Param("chgs") List<MoneyBalanceChg> chgs);

}