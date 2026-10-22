package api.poja.app.service;

import api.poja.app.endpoint.rest.model.request.CreateLicenseKeyRequest;
import api.poja.app.endpoint.rest.model.response.GeneratedLicenseResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationItem;
import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import api.poja.app.endpoint.rest.model.response.LicenseKeyResponse;
import api.poja.app.endpoint.rest.model.response.LicenseStatus;
import api.poja.app.exception.ApiException;
import api.poja.app.repository.JLicenseActivationRepository;
import api.poja.app.repository.JLicenseKeyRepository;
import api.poja.app.repository.JMemberRepository;
import api.poja.app.repository.model.JLicenseActivation;
import api.poja.app.repository.model.JLicenseKey;
import api.poja.app.repository.model.JMember;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class LicenseService {

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int DEFAULT_MAX_ACTIVATIONS = 1;
  private static final int MAX_ACTIVATIONS_LIMIT = 1000;

  private final JLicenseKeyRepository keyRepository;
  private final JLicenseActivationRepository activationRepository;
  private final JMemberRepository memberRepository;
  private final Clock clock;

  @Transactional
  public LicenseActivationResponse activate(JMember member, String rawKey) {
    if (rawKey == null || rawKey.isBlank()) {
      return new LicenseActivationResponse(false, "Veuillez renseigner ce champ.", null);
    }
    var key = keyRepository.findByCodeHash(hash(rawKey)).orElse(null);
    if (key == null) {
      return new LicenseActivationResponse(false, "Clé invalide.", null);
    }
    if (!key.isActive()) {
      return new LicenseActivationResponse(false, "Clé expirée.", null);
    }
    if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(clock.instant())) {
      return new LicenseActivationResponse(false, "Clé expirée.", null);
    }

    var existing = activationRepository.findByLicenseKeyIdAndMemberId(key.getId(), member.getId());
    if (existing.isPresent()) {
      var activation = existing.get();
      if (activation.getRevokedAt() == null) {
        return new LicenseActivationResponse(
            true, "Clé déjà activée sur ce compte.", key.getExpiresAt());
      }
      activation.setRevokedAt(null);
      activation.setActivatedAt(clock.instant());
      activationRepository.save(activation);
      refreshActivationCount(key);
      return new LicenseActivationResponse(true, "Clé activée. Bon trading !", key.getExpiresAt());
    }

    var activeCount = activationRepository.countByLicenseKeyIdAndRevokedAtIsNull(key.getId());
    if (activeCount >= key.getMaxActivations()) {
      return new LicenseActivationResponse(
          false, "Cette clé a atteint sa limite d'activations.", key.getExpiresAt());
    }

    activationRepository.save(
        JLicenseActivation.builder()
            .licenseKeyId(key.getId())
            .memberId(member.getId())
            .activatedAt(clock.instant())
            .build());
    refreshActivationCount(key);
    return new LicenseActivationResponse(true, "Clé activée. Bon trading !", key.getExpiresAt());
  }

  @Transactional(readOnly = true)
  public LicenseStatus statusFor(UUID memberId) {
    var activation =
        activationRepository.findFirstByMemberIdAndRevokedAtIsNullOrderByActivatedAtDesc(memberId);
    if (activation.isEmpty()) {
      return LicenseStatus.inactive();
    }
    var key = keyRepository.findById(activation.get().getLicenseKeyId()).orElse(null);
    if (key == null || !key.isActive()) {
      return LicenseStatus.inactive();
    }
    if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(clock.instant())) {
      return LicenseStatus.inactive();
    }
    return new LicenseStatus(true, key.getCodePrefix(), key.getLabel(), key.getExpiresAt());
  }

  public boolean hasActiveLicense(JMember member) {
    return member != null && statusFor(member.getId()).active();
  }

  @Transactional
  public GeneratedLicenseResponse generate(CreateLicenseKeyRequest request, UUID createdBy) {
    var maxActivations =
        request == null || request.maxActivations() == null
            ? DEFAULT_MAX_ACTIVATIONS
            : request.maxActivations();
    if (maxActivations < 1 || maxActivations > MAX_ACTIVATIONS_LIMIT) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "Le nombre d'activations doit être entre 1 et 1000");
    }
    var expiresAt = request == null ? null : request.expiresAt();
    if (expiresAt != null && expiresAt.isBefore(clock.instant())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "La date d'expiration est déjà passée");
    }
    var label = request == null ? null : trimToNull(request.label());

    for (var attempt = 0; attempt < 5; attempt++) {
      var code = newCode();
      var codeHash = hash(code);
      if (keyRepository.findByCodeHash(codeHash).isPresent()) {
        continue;
      }
      var entity =
          keyRepository.save(
              JLicenseKey.builder()
                  .codeHash(codeHash)
                  .codePrefix(prefix(code))
                  .label(label)
                  .maxActivations(maxActivations)
                  .activationCount(0)
                  .expiresAt(expiresAt)
                  .active(true)
                  .createdAt(clock.instant())
                  .createdBy(createdBy)
                  .build());
      return new GeneratedLicenseResponse(code, toResponse(entity));
    }
    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de générer la clé");
  }

  @Transactional(readOnly = true)
  public List<LicenseKeyResponse> keys() {
    return keyRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
  }

  @Transactional
  public void revoke(UUID keyId) {
    var key = keyRepository.findById(keyId).orElseThrow(this::notFound);
    key.setActive(false);
    keyRepository.save(key);
  }

  @Transactional(readOnly = true)
  public List<LicenseActivationItem> activations(UUID keyId) {
    if (!keyRepository.existsById(keyId)) {
      throw notFound();
    }
    return activationRepository.findAllByLicenseKeyIdOrderByActivatedAtDesc(keyId).stream()
        .map(this::toItem)
        .toList();
  }

  @Transactional
  public void revokeActivation(UUID activationId) {
    var activation = activationRepository.findById(activationId).orElseThrow(this::notFound);
    if (activation.getRevokedAt() == null) {
      activation.setRevokedAt(clock.instant());
      activationRepository.save(activation);
      keyRepository.findById(activation.getLicenseKeyId()).ifPresent(this::refreshActivationCount);
    }
  }

  @Transactional(readOnly = true)
  public List<LicenseActivationItem> activationsForMember(UUID memberId) {
    return activationRepository.findAllByMemberIdOrderByActivatedAtDesc(memberId).stream()
        .map(this::toItem)
        .toList();
  }

  @Transactional
  public void revokeMemberLicenses(UUID memberId) {
    activationRepository.findAllByMemberIdOrderByActivatedAtDesc(memberId).stream()
        .filter(activation -> activation.getRevokedAt() == null)
        .forEach(
            activation -> {
              activation.setRevokedAt(clock.instant());
              activationRepository.save(activation);
              keyRepository
                  .findById(activation.getLicenseKeyId())
                  .ifPresent(this::refreshActivationCount);
            });
  }

  private void refreshActivationCount(JLicenseKey key) {
    var count = activationRepository.countByLicenseKeyIdAndRevokedAtIsNull(key.getId());
    key.setActivationCount((int) count);
    keyRepository.save(key);
  }

  private LicenseKeyResponse toResponse(JLicenseKey key) {
    var activeCount = activationRepository.countByLicenseKeyIdAndRevokedAtIsNull(key.getId());
    return new LicenseKeyResponse(
        key.getId(),
        key.getCodePrefix(),
        key.getLabel(),
        key.getMaxActivations(),
        activeCount,
        key.getExpiresAt(),
        key.isActive(),
        key.getCreatedAt());
  }

  private LicenseActivationItem toItem(JLicenseActivation activation) {
    var email =
        memberRepository.findById(activation.getMemberId()).map(JMember::getEmail).orElse(null);
    return new LicenseActivationItem(
        activation.getId(),
        activation.getMemberId(),
        email,
        activation.getActivatedAt(),
        activation.getRevokedAt());
  }

  private String newCode() {
    var random = new SecureRandom();
    var builder = new StringBuilder("ASK");
    for (var group = 0; group < 3; group++) {
      builder.append('-');
      for (var index = 0; index < 4; index++) {
        builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
      }
    }
    return builder.toString();
  }

  private String prefix(String code) {
    return code.length() <= 8 ? code : code.substring(0, 8) + "…";
  }

  private String hash(String rawKey) {
    try {
      var normalized = rawKey.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
      var digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to hash license key", exception);
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Clé introuvable");
  }
}
