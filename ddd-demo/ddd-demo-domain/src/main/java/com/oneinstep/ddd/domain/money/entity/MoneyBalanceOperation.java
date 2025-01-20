package com.oneinstep.ddd.domain.money.entity;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import com.oneinstep.ddd.domain.money.constant.MoneyBalanceFlagBitConstants;
import com.oneinstep.ddd.domain.money.constant.MoneyBalanceOperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包操作记录 领域对象
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "money_balance_operation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_operation_from_source", columnNames = {"fromSourceType", "fromSourceId",
                "fromSourceSubId"})})
public class MoneyBalanceOperation {

    /**
     * 操作记录ID
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
     * 操作类型
     */
    @Column(name = "operation_type", nullable = false)
    private Integer operationType;

    /**
     * 操作金额
     */
    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(10, 6) DEFAULT 0.000000")
    private BigDecimal amount;

    /**
     * 支付方式 0-单币种支付 1-组合支付
     */
    @Column(name = "payment_type", nullable = false)
    private Integer paymentType;
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
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime operationTime;

    /**
     * 备注
     */
    @Column(name = "remark", nullable = true)
    private String remark;

    /**
     * 标记位 00000000000000000000000000000000
     */
    @Column(name = "flag_bit", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long flagBit;

    /**
     * 添加标记位
     *
     * @param flagBit 标记位
     */
    public void addFlagBit(long flagBit) {
        if (this.flagBit == null) {
            this.flagBit = flagBit;
        } else {
            this.flagBit |= flagBit;
        }
    }

    /**
     * 存入资金
     *
     * @param money 操作金额
     * @return 操作记录
     */
    public static MoneyBalanceOperation newDepositOperation(Long assetAccountId, Integer moneyType,
                                                            MoneyAmount money, FromSource fromSource) {
        MoneyBalanceOperation operation = new MoneyBalanceOperation();
        operation.assetAccountId = assetAccountId;
        operation.moneyType = moneyType;
        operation.amount = money.amount();
        operation.operationType = MoneyBalanceOperationType.INCREASE.getCode();
        operation.operationTime = LocalDateTime.now();
        operation.remark = "存入资金";

        // 单币种支付 不校验
        operation.paymentType = 0;

        operation.fromSourceType = fromSource.fromSourceType();
        operation.fromSourceId = fromSource.fromSourceId();
        operation.fromSourceSubId = fromSource.fromSourceSubId();

        operation.addFlagBit(MoneyBalanceFlagBitConstants.INCREASE_CURRENT_BALANCE);

        return operation;
    }

    /**
     * 取现
     *
     * @param money 操作金额
     * @return 操作记录
     */
    public static MoneyBalanceOperation newWithdrawOperation(Long assetAccountId,
                                                             MoneyAmount money,
                                                             PaymentMethod paymentMethod,
                                                             FromSource fromSource) {
        MoneyBalanceOperation operation = new MoneyBalanceOperation();
        operation.assetAccountId = assetAccountId;
        operation.moneyType = money.currency().code();
        operation.amount = money.amount();
        operation.operationType = MoneyBalanceOperationType.DECREASE.getCode();
        operation.operationTime = LocalDateTime.now();
        operation.remark = "取现";

        operation.paymentType = paymentMethod.multipleCurrency() ? 1 : 0;

        operation.fromSourceType = fromSource.fromSourceType();
        operation.fromSourceId = fromSource.fromSourceId();
        operation.fromSourceSubId = fromSource.fromSourceSubId();

        operation.addFlagBit(MoneyBalanceFlagBitConstants.DECREASE_CURRENT_BALANCE);

        return operation;
    }

}
