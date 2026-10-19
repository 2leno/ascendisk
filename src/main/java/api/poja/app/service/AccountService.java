package api.poja.app.service;

import api.poja.app.endpoint.rest.model.request.AccountUpdate;
import api.poja.app.exception.ApiException;
import api.poja.app.mapper.AccountMapper;
import api.poja.app.model.Account;
import api.poja.app.repository.JAccountRepository;
import java.time.Clock;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class AccountService {

  private final JAccountRepository accountRepository;
  private final AccountMapper accountMapper;
  private final Clock clock;

  @Transactional(readOnly = true)
  public Account get(String username) {
    return accountMapper.toDomain(
        accountRepository.findByUsername(username).orElseThrow(this::notFound));
  }

  @Transactional
  public Account update(String username, AccountUpdate input) {
    if (input == null || input.balance() == null || input.balance() < 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Balance must not be negative");
    }
    var account = accountRepository.findByUsername(username).orElseThrow(this::notFound);
    account.setBalance(input.balance());
    account.setUpdatedAt(clock.instant());
    return accountMapper.toDomain(accountRepository.save(account));
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Account not found");
  }
}
