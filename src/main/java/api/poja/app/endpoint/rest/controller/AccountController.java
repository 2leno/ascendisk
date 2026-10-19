package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.AccountUpdate;
import api.poja.app.endpoint.rest.model.response.AccountResponse;
import api.poja.app.mapper.AccountMapper;
import api.poja.app.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AccountController {

  private final AccountService accountService;
  private final AccountMapper accountMapper;

  @GetMapping("/account")
  public AccountResponse get(Authentication authentication) {
    return accountMapper.toResponse(accountService.get(authentication.getName()));
  }

  @PutMapping("/account")
  public AccountResponse update(Authentication authentication, @RequestBody AccountUpdate input) {
    return accountMapper.toResponse(accountService.update(authentication.getName(), input));
  }
}
