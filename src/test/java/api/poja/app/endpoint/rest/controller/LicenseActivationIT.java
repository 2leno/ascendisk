package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.endpoint.rest.model.request.CreateLicenseKeyRequest;
import api.poja.app.endpoint.rest.model.response.GeneratedLicenseResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import api.poja.app.repository.JLicenseKeyRepository;
import api.poja.app.repository.JMemberRepository;
import api.poja.app.service.LicenseService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LicenseActivationIT extends ApiIT {

  @Autowired LicenseService licenseService;

  @Autowired JMemberRepository memberRepository;

  @Autowired JLicenseKeyRepository licenseKeyRepository;

  @Test
  void activate_withGeneratedKey_succeeds() {
    var code = generateKey(1).code();
    var email = uniqueEmail();
    var response = activate(email, code.toLowerCase());

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().success());
    var member = memberRepository.findByEmail(email).orElseThrow();
    assertTrue(licenseService.hasActiveLicense(member));
  }

  @Test
  void activate_withWrongKey_fails() {
    var response = activate(uniqueEmail(), "OBK-WRONG");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().success());
    assertEquals("Clé invalide.", response.getBody().message());
  }

  @Test
  void activate_sameKeyTwice_isIdempotent() {
    var code = generateKey(1).code();
    var email = uniqueEmail();
    activate(email, code);
    var response = activate(email, code);

    assertTrue(response.getBody().success());
    var member = memberRepository.findByEmail(email).orElseThrow();
    var activations = licenseService.activationsForMember(member.getId());
    assertEquals(1, activations.stream().filter(item -> item.revokedAt() == null).count());
  }

  @Test
  void activate_beyondActivationLimit_fails() {
    var code = generateKey(1).code();
    assertTrue(activate(uniqueEmail(), code).getBody().success());

    var response = activate(uniqueEmail(), code);

    assertFalse(response.getBody().success());
    assertTrue(response.getBody().message().contains("limite"));
  }

  @Test
  void activate_expiredKey_fails() {
    var generated = generateKey(1);
    var key = licenseKeyRepository.findById(generated.key().id()).orElseThrow();
    key.setExpiresAt(Instant.now().minusSeconds(60));
    licenseKeyRepository.save(key);

    var response = activate(uniqueEmail(), generated.code());

    assertFalse(response.getBody().success());
    assertEquals("Clé expirée.", response.getBody().message());
  }

  @Test
  void activate_revokedKey_fails() {
    var generated = generateKey(1);
    licenseService.revoke(generated.key().id());

    var response = activate(uniqueEmail(), generated.code());

    assertFalse(response.getBody().success());
    assertEquals("Clé expirée.", response.getBody().message());
  }

  private GeneratedLicenseResponse generateKey(int maxActivations) {
    return licenseService.generate(
        new CreateLicenseKeyRequest("IT " + UUID.randomUUID(), maxActivations, null), null);
  }

  private ResponseEntity<LicenseActivationResponse> activate(String email, String key) {
    var response =
        post(
            "/api/licenses/activate",
            firebaseToken(email),
            Map.of("key", key),
            LicenseActivationResponse.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    return response;
  }

  private String uniqueEmail() {
    return "buyer-" + UUID.randomUUID() + "@test.local";
  }
}
