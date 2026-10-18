package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.AccountResponse;
import api.poja.app.model.Account;
import api.poja.app.repository.model.JAccount;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

  public Account toDomain(JAccount account) {
    return Account.builder()
        .id(account.getId())
        .username(account.getUsername())
        .balance(account.getBalance())
        .currency(account.getCurrency())
        .updatedAt(account.getUpdatedAt())
        .build();
  }

  public AccountResponse toResponse(Account account) {
    return AccountResponse.builder()
        .username(account.username())
        .balance(account.balance())
        .currency(account.currency())
        .updatedAt(account.updatedAt())
        .build();
  }
}
