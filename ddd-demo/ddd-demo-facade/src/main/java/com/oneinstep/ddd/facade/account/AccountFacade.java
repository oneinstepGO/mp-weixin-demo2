package com.oneinstep.ddd.facade.account;

import com.oneinstep.ddd.facade.account.dto.AccountDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 账户防腐层
 * 隔绝外部依赖，提供内部调用接口
 */
@Component
public class AccountFacade {

    public AccountDTO getAccountByAssetAccountId(Long assetAccountId) {
        // 从帐户RPC中获取账户信息 but mock here
        if (Objects.equals(assetAccountId, 1L)) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setAccountId(1L);
            accountDTO.setAccountName("account1");
            accountDTO.setAssetAccountId(1L);
            accountDTO.setAssetAccountType(1);
            return accountDTO;
        } else if (Objects.equals(assetAccountId, 2L)) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setAccountId(2L);
            accountDTO.setAccountName("account2");
            accountDTO.setAssetAccountId(2L);
            accountDTO.setAssetAccountType(2);
            return accountDTO;
        }
        return null;
    }

    public AccountDTO getAccountByAccountId(Long accountId) {
        // 从帐户RPC中获取账户信息 but mock here
        if (Objects.equals(accountId, 1L)) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setAccountId(1L);
            accountDTO.setAccountName("account1");
            accountDTO.setAssetAccountId(1L);
            accountDTO.setAssetAccountType(1);
            return accountDTO;
        } else if (Objects.equals(accountId, 2L)) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setAccountId(2L);
            accountDTO.setAccountName("account2");
            accountDTO.setAssetAccountId(2L);
            accountDTO.setAssetAccountType(2);
            return accountDTO;
        }
        return null;
    }

}
