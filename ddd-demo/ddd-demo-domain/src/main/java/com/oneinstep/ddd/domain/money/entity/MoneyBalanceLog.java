package com.oneinstep.ddd.domain.money.entity;

import com.oneinstep.ddd.domain.money.constant.MoneyBalanceFlagBitConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包变动日志 领域对象
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "money_balance_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_balance_log_from_source", columnNames = {"fromSourceType", "fromSourceId",
                "fromSourceSubId"})})
public class MoneyBalanceLog {

    /**
     * 钱包变动日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资金账户ID
     */
    @Column(name = "asset_account_id", nullable = false)
    private Long assetAccountId;

    /**
     * 币种类型
     */
    @Column(name = "money_type", nullable = false)
    private Integer moneyType;

    /**
     * 钱包ID
     */
    @Column(name = "money_balance_id", nullable = false)
    private Long moneyBalanceId;

    /**
     * 余额变动
     */
    @Column(name = "chg_current_balance", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal chgCurrentBalance;

    /**
     * 操作前余额
     */
    @Column(name = "before_current_balance", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal beforeCurrentBalance;

    /**
     * 操作后余额
     */
    @Column(name = "after_current_balance", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal afterCurrentBalance;

    /**
     * 冻结金额变动
     */
    @Column(name = "chg_frozen_amount", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal chgFrozenAmount;

    /**
     * 操作前冻结金额
     */
    @Column(name = "before_frozen_amount", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal beforeFrozenAmount;

    /**
     * 操作后冻结金额
     */
    @Column(name = "after_frozen_amount", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal afterFrozenAmount;


    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime operationTime;

    /**
     * 标记位 00000000000000000000000000000000
     * 每一位表示一个标记 0表示未标记,1表示已标记 可以存储64个标记
     */
    @Column(name = "flag_bit", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long flagBit = 0L;

    /**
     * 来源类型-用于幂等
     */
    @Column(name = "from_source_type", nullable = false)
    private Integer fromSourceType;

    /**
     * 来源ID-用于幂等
     */
    @Column(name = "from_source_id", nullable = false)
    private Long fromSourceId;

    /**
     * 来源子ID-用于幂等
     */
    @Column(name = "from_source_sub_id", nullable = false)
    private Long fromSourceSubId;

    /**
     * 添加标记位
     *
     * @param flagBit 标记位
     */
    public void addFlagBit(long flagBit) {
        if (this.flagBit == null) {
            this.flagBit = 0L;
        }
        this.flagBit = this.flagBit | flagBit;
    }

    /**
     * 判断标记位是否已设置
     *
     * @param flagBit 标记位
     * @return 是否已设置
     */
    public boolean isFlagBitSet(long flagBit) {
        return (this.flagBit & flagBit) != 0;
    }

    /**
     * /**
     * 从操作记录创建资金变动流水
     *
     * @param newBalance 操作后的余额
     * @param oldBalance 操作前的余额
     * @return 资金变动流水
     */
    public static MoneyBalanceLog createMoneyBalanceLog(MoneyBalance newBalance,
                                                        MoneyBalance oldBalance, Integer fromSourceType) {
        MoneyBalanceLog moneyBalanceLog = new MoneyBalanceLog();
        moneyBalanceLog.setAssetAccountId(oldBalance.getAssetAccountId());
        moneyBalanceLog.setMoneyType(oldBalance.getMoneyType());
        moneyBalanceLog.setMoneyBalanceId(oldBalance.getId());
        moneyBalanceLog.setChgCurrentBalance(newBalance.getCurrentBalance().subtract(oldBalance.getCurrentBalance()));
        moneyBalanceLog.setBeforeCurrentBalance(oldBalance.getCurrentBalance());
        moneyBalanceLog.setAfterCurrentBalance(newBalance.getCurrentBalance());
        moneyBalanceLog.setChgFrozenAmount(newBalance.getFrozenAmount().subtract(oldBalance.getFrozenAmount()));
        moneyBalanceLog.setBeforeFrozenAmount(oldBalance.getFrozenAmount());
        moneyBalanceLog.setAfterFrozenAmount(newBalance.getFrozenAmount());
        moneyBalanceLog.setFromSourceType(fromSourceType);
        // 设置标记位
        if (moneyBalanceLog.getChgCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            moneyBalanceLog.addFlagBit(MoneyBalanceFlagBitConstants.DECREASE_CURRENT_BALANCE);
        } else if (moneyBalanceLog.getChgCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            moneyBalanceLog.addFlagBit(MoneyBalanceFlagBitConstants.INCREASE_CURRENT_BALANCE);
        }

        if (moneyBalanceLog.getChgFrozenAmount().compareTo(BigDecimal.ZERO) < 0) {
            moneyBalanceLog.addFlagBit(MoneyBalanceFlagBitConstants.DECREASE_FREEZE_AMOUNT);
        } else if (moneyBalanceLog.getChgFrozenAmount().compareTo(BigDecimal.ZERO) < 0) {
            moneyBalanceLog.addFlagBit(MoneyBalanceFlagBitConstants.INCREASE_FREEZE_AMOUNT);
        }

        moneyBalanceLog.setOperationTime(LocalDateTime.now());
        return moneyBalanceLog;
    }

}
