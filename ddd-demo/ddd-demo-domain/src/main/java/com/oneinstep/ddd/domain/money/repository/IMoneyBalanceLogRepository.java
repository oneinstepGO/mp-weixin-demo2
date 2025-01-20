package com.oneinstep.ddd.domain.money.repository;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;

import java.util.List;

public interface IMoneyBalanceLogRepository {

    /**
     * 根据来源类型、来源ID查询资金变动记录
     *
     * @param fromSourceType 来源类型
     * @param fromSourceId   来源ID
     * @return 资金变动记录
     */
    List<MoneyBalanceLog> findLogsBy2F(Integer fromSourceType, Long fromSourceId);

    /**
     * 根据来源类型、来源ID、来源子ID查询资金变动记录
     *
     * @param fromSourceType  来源类型
     * @param fromSourceId    来源ID
     * @param fromSourceSubId 来源子ID
     * @return 资金变动记录
     */
    MoneyBalanceLog findBy3F(FromSource fromSource);

    /**
     * 保存资金变动记录
     *
     * @param moneyBalanceLog 资金变动记录
     * @return 保存结果
     */
    boolean saveLog(MoneyBalanceLog moneyBalanceLog);

    /**
     * 批量保存资金变动记录
     *
     * @param moneyBalanceLogs 资金变动记录列表
     * @return 保存结果
     */
    boolean saveLogBatch(List<MoneyBalanceLog> moneyBalanceLogs);
}
