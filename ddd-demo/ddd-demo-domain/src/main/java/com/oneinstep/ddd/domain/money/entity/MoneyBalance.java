package com.oneinstep.ddd.domain.money.entity;

import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.domain.money.exception.MoneyNotEnoughException;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资金钱包 领域对象
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "money_balance")
public class MoneyBalance {

    /**
     * 钱包ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资金帐户id
     */
    @Column(name = "asset_account_id", nullable = false)
    private Long assetAccountId;

    /**
     * 币种类型
     */
    @Column(name = "money_type", nullable = false)
    private int moneyType;

    /**
     * 当前余额
     */
    @Column(name = "current_balance", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal currentBalance;

    /**
     * 冻结金额
     */
    @Column(name = "frozen_amount", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal frozenAmount;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long version;

    public MoneyBalance() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.version = 0L;
        this.currentBalance = BigDecimal.ZERO;
        this.frozenAmount = BigDecimal.ZERO;
    }

    /**
     * 存钱
     *
     * @param money 金额
     */
    public void deposit(@Nonnull MoneyAmount money) {
        if (money.amount() == null) {
            throw new IllegalArgumentException("Invalid money");
        }
        if (money.currency().code() != this.moneyType) {
            throw new IllegalArgumentException("Money type mismatch");
        }
        this.currentBalance = this.currentBalance.add(money.amount());
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 扣款
     *
     * @param money 金额
     */
    public void withdraw(@Nonnull MoneyAmount money) {
        if (money.amount() == null) {
            throw new IllegalArgumentException("Invalid money");
        }
        if (money.currency().code() != this.moneyType) {
            throw new IllegalArgumentException("Money type mismatch");
        }
        if (!hasSufficientFunds(money.amount())) {
            throw new MoneyNotEnoughException("Insufficient funds");
        }

        this.currentBalance = this.currentBalance.subtract(money.amount());
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 是否有足够的余额
     *
     * @param amount 金额
     * @return 是否有足够的余额
     */
    public boolean hasSufficientFunds(BigDecimal amount) {
        return this.currentBalance.compareTo(amount) >= 0;
    }

    /**
     * 复制
     *
     * @return 复制后的钱包
     */
    public MoneyBalance copy() {
        MoneyBalance moneyBalance = new MoneyBalance();
        BeanUtils.copyProperties(this, moneyBalance);
        moneyBalance.setCreateTime(this.createTime);
        moneyBalance.setUpdateTime(LocalDateTime.now());
        return moneyBalance;
    }

    public static MoneyBalance findBalance(List<MoneyBalance> moneyBalances, Integer moneyType) {
        return moneyBalances.stream().filter(moneyBalance -> moneyBalance.getMoneyType() == moneyType).findFirst()
                .orElse(null);
    }

}
