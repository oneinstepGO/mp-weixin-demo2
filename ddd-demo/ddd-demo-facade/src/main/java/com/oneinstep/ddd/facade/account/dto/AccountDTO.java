package com.oneinstep.ddd.facade.account.dto;

import lombok.Data;

/**
 * 账户DTO
 */
@Data
public class AccountDTO {

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户类型
     */
    private int assetAccountType;

    /**
     * 资产帐户ID
     */
    private Long assetAccountId;
}
