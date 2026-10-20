package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.AssetInput;
import api.poja.app.endpoint.rest.model.request.CreateLicenseKeyRequest;
import api.poja.app.endpoint.rest.model.response.AdminOverviewResponse;
import api.poja.app.endpoint.rest.model.response.AssetResponse;
import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.endpoint.rest.model.response.GeneratedLicenseResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationItem;
import api.poja.app.endpoint.rest.model.response.LicenseKeyResponse;
import api.poja.app.endpoint.rest.model.response.MemberResponse;
import api.poja.app.mapper.AssetMapper;
import api.poja.app.mapper.FrontendSignalMapper;
import api.poja.app.model.MemberStatus;
import api.poja.app.repository.JAssetRepository;
import api.poja.app.repository.JLicenseActivationRepository;
import api.poja.app.repository.JLicenseKeyRepository;
import api.poja.app.repository.JMemberRepository;
import api.poja.app.security.FirebaseUser;
import api.poja.app.service.AssetService;
import api.poja.app.service.LicenseService;
import api.poja.app.service.MemberService;
import api.poja.app.service.SignalService;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') and principal instanceof T(api.poja.app.security.FirebaseUser)")
@AllArgsConstructor
public class AdminController {

  private final MemberService memberService;
  private final LicenseService licenseService;
  private final AssetService assetService;
  private final AssetMapper assetMapper;
  private final SignalService signalService;
  private final FrontendSignalMapper frontendSignalMapper;
  private final JMemberRepository memberRepository;
  private final JLicenseKeyRepository licenseKeyRepository;
  private final JLicenseActivationRepository licenseActivationRepository;
  private final JAssetRepository assetRepository;

  @GetMapping("/overview")
  public AdminOverviewResponse overview() {
    return new AdminOverviewResponse(
        memberRepository.count(),
        memberRepository.countByStatus(MemberStatus.ACTIVE),
        licenseKeyRepository.count(),
        licenseKeyRepository.countByActiveTrue(),
        licenseActivationRepository.countByRevokedAtIsNull(),
        assetRepository.count(),
        assetRepository.countByEnabledTrue(),
        assetRepository.countByEnabledTrue());
  }

  @GetMapping("/assets")
  public List<AssetResponse> assets() {
    return assetService.list(null).stream().map(assetMapper::toResponse).toList();
  }

  @PostMapping("/assets")
  @ResponseStatus(HttpStatus.CREATED)
  public AssetResponse createAsset(@RequestBody AssetInput input) {
    return assetMapper.toResponse(assetService.create(input));
  }

  @PutMapping("/assets/{symbol}")
  public AssetResponse updateAsset(@PathVariable String symbol, @RequestBody AssetInput input) {
    return assetMapper.toResponse(assetService.update(symbol, input));
  }

  @GetMapping("/signals")
  public List<FrontendSignalResponse> signals() {
    var signals = signalService.currentSignals();
    return IntStream.range(0, signals.size())
        .mapToObj(index -> frontendSignalMapper.toResponse(signals.get(index), index + 1L))
        .toList();
  }

  @GetMapping("/keys")
  public List<LicenseKeyResponse> keys() {
    return licenseService.keys();
  }

  @PostMapping("/keys")
  @ResponseStatus(HttpStatus.CREATED)
  public GeneratedLicenseResponse generateKey(
      Authentication authentication,
      @RequestBody(required = false) CreateLicenseKeyRequest request) {
    return licenseService.generate(request, adminId(authentication));
  }

  @PostMapping("/keys/{id}/revoke")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeKey(@PathVariable UUID id) {
    licenseService.revoke(id);
  }

  @GetMapping("/keys/{id}/activations")
  public List<LicenseActivationItem> keyActivations(@PathVariable UUID id) {
    return licenseService.activations(id);
  }

  @PostMapping("/activations/{id}/revoke")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeActivation(@PathVariable UUID id) {
    licenseService.revokeActivation(id);
  }

  @GetMapping("/members")
  public List<MemberResponse> members() {
    return memberService.list();
  }

  @PostMapping("/members/{id}/block")
  public MemberResponse blockMember(@PathVariable UUID id) {
    return memberService.setBlocked(id, true);
  }

  @PostMapping("/members/{id}/unblock")
  public MemberResponse unblockMember(@PathVariable UUID id) {
    return memberService.setBlocked(id, false);
  }

  @GetMapping("/members/{id}/activations")
  public List<LicenseActivationItem> memberActivations(@PathVariable UUID id) {
    return licenseService.activationsForMember(id);
  }

  @PostMapping("/members/{id}/license/revoke")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeMemberLicenses(@PathVariable UUID id) {
    licenseService.revokeMemberLicenses(id);
  }

  private UUID adminId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof FirebaseUser user) {
      return memberService.requireAdmin(user).getId();
    }
    return null;
  }
}
