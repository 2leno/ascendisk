package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.LicenseActivationRequest;
import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import api.poja.app.endpoint.rest.model.response.MemberResponse;
import api.poja.app.exception.ApiException;
import api.poja.app.mapper.FrontendSignalMapper;
import api.poja.app.security.FirebaseUser;
import api.poja.app.service.LicenseService;
import api.poja.app.service.MemberService;
import api.poja.app.service.SignalAccessPolicy;
import api.poja.app.service.SignalService;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FrontendController {

  private final SignalService signalService;
  private final FrontendSignalMapper frontendSignalMapper;
  private final LicenseService licenseService;
  private final MemberService memberService;
  private final SignalAccessPolicy signalAccessPolicy;

  @GetMapping("/signals")
  public List<FrontendSignalResponse> signals(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof FirebaseUser user) {
      memberService.requireNotBlocked(user);
    }
    var signals = signalService.currentSignals();
    var premium = hasPremiumAccess(authentication);
    return IntStream.range(0, signals.size())
        .filter(index -> signalAccessPolicy.visible(signals.get(index), premium))
        .mapToObj(
            index ->
                frontendSignalMapper.toResponse(signals.get(index), index + 1L, premium))
        .toList();
  }

  @GetMapping("/me")
  public MemberResponse me(Authentication authentication) {
    var user = firebaseUser(authentication);
    return memberService.responseFor(memberService.requireActive(user));
  }

  @PostMapping("/licenses/activate")
  public LicenseActivationResponse activate(
      Authentication authentication,
      @RequestBody(required = false) LicenseActivationRequest request) {
    var member = memberService.requireActive(firebaseUser(authentication));
    return licenseService.activate(member, request == null ? null : request.key());
  }

  private boolean hasPremiumAccess(Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    if (authentication.getPrincipal() instanceof FirebaseUser user) {
      return memberService.hasPremiumAccess(user);
    }
    return authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_TRADER"));
  }

  private FirebaseUser firebaseUser(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof FirebaseUser user) {
      return user;
    }
    throw new ApiException(HttpStatus.UNAUTHORIZED, "Connexion Firebase requise");
  }
}
