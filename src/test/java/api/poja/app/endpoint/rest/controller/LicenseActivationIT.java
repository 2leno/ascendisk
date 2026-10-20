package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.license.keys=OBK-TEST-KEY")
class LicenseActivationIT extends ApiIT {

  @Test
  void activate_withConfiguredKey_succeeds() {
    var response =
        post(
            "/api/licenses/activate",
            null,
            Map.of("key", "obk-test-key"),
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().success());
  }

  @Test
  void activate_withWrongKey_fails() {
    var response =
        post(
            "/api/licenses/activate",
            null,
            Map.of("key", "OBK-WRONG"),
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().success());
  }
}
