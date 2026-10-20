package api.poja.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.request.CreateLicenseKeyRequest;
import api.poja.app.exception.ApiException;
import api.poja.app.repository.JLicenseActivationRepository;
import api.poja.app.repository.JLicenseKeyRepository;
import api.poja.app.repository.JMemberRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LicenseServiceTest {

  private final JLicenseKeyRepository keyRepository = mock(JLicenseKeyRepository.class);
  private final JLicenseActivationRepository activationRepository =
      mock(JLicenseActivationRepository.class);
  private final JMemberRepository memberRepository = mock(JMemberRepository.class);
  private LicenseService service;

  @BeforeEach
  void setUp() {
    service =
        new LicenseService(
            keyRepository,
            activationRepository,
            memberRepository,
            Clock.fixed(Instant.parse("2024-01-03T12:00:00Z"), ZoneOffset.UTC));
  }

  @Test
  void activate_withoutKey_returnsFailure() {
    var response = service.activate(null, null);

    assertFalse(response.success());
    assertEquals("Entre ta clé d'activation.", response.message());
  }

  @Test
  void activate_withBlankKey_returnsFailure() {
    var response = service.activate(null, "  ");

    assertFalse(response.success());
    assertEquals("Entre ta clé d'activation.", response.message());
  }

  @Test
  void activate_withUnknownKey_returnsFailure() {
    when(keyRepository.findByCodeHash(any())).thenReturn(Optional.empty());

    var response = service.activate(null, "OBK-WRONG");

    assertFalse(response.success());
    assertEquals("Clé invalide.", response.message());
  }

  @Test
  void generate_returnsReadableCode() {
    when(keyRepository.findByCodeHash(any())).thenReturn(Optional.empty());
    when(keyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var generated = service.generate(new CreateLicenseKeyRequest("Test", 1, null), null);

    assertTrue(generated.code().matches("ASC(-[A-Z2-9]{4}){3}"));
    assertEquals(1, generated.key().maxActivations());
  }

  @Test
  void generate_withInvalidActivationCount_throwsBadRequest() {
    var exception =
        assertThrows(
            ApiException.class,
            () -> service.generate(new CreateLicenseKeyRequest("Test", 0, null), null));

    assertEquals(400, exception.getStatus().value());
  }
}
