package api.poja.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LicenseServiceTest {

  @Test
  void activate_withoutKey_returnsFailure() {
    var response = new LicenseService("OBK-TEST").activate(null);

    assertFalse(response.success());
    assertEquals("Entre ta clé d'activation.", response.message());
  }

  @Test
  void activate_withBlankKey_returnsFailure() {
    var response = new LicenseService("OBK-TEST").activate("  ");

    assertFalse(response.success());
    assertEquals("Entre ta clé d'activation.", response.message());
  }

  @Test
  void activate_withoutConfiguredKeys_returnsFailure() {
    var response = new LicenseService("").activate("OBK-TEST");

    assertFalse(response.success());
    assertEquals("Aucune clé active pour le moment.", response.message());
  }

  @Test
  void activate_withConfiguredKey_isCaseInsensitive() {
    var response = new LicenseService(" obk-test , OBK-OTHER ").activate("Obk-Test");

    assertTrue(response.success());
    assertEquals("Clé activée. Bon trading !", response.message());
  }

  @Test
  void activate_withUnknownKey_returnsFailure() {
    var response = new LicenseService("OBK-TEST").activate("OBK-WRONG");

    assertFalse(response.success());
    assertEquals("Clé invalide.", response.message());
  }
}
