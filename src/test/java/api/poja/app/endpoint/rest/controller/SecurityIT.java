package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

class SecurityIT extends ApiIT {

  @Test
  void trades_withoutToken_returnsUnauthorized() {
    assertEquals(HttpStatus.UNAUTHORIZED, get("/trades", null, String.class).getStatusCode());
  }

  @Test
  void trades_withInvalidToken_returnsUnauthorized() {
    assertEquals(
        HttpStatus.UNAUTHORIZED, get("/trades", "invalid-token", String.class).getStatusCode());
  }

  @Test
  void trades_withValidToken_isAuthorized() {
    var status = get("/trades", token("TRADER"), String.class).getStatusCode();
    assertNotEquals(HttpStatus.UNAUTHORIZED, status);
    assertNotEquals(HttpStatus.FORBIDDEN, status);
  }

  @Test
  void ping_isPublic() {
    var response = get("/ping", null, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("pong", response.getBody());
  }

  @Test
  void corsPreflight_allowsFrontendOrigin() {
    assertPreflightAllowed("http://localhost:2042");
  }

  @Test
  void corsPreflight_allowsPagesOrigin() {
    assertPreflightAllowed("https://ascendisk.pages.dev");
  }

  private void assertPreflightAllowed(String origin) {
    var headers = new HttpHeaders();
    headers.setOrigin(origin);
    headers.setAccessControlRequestMethod(HttpMethod.GET);
    headers.setAccessControlRequestHeaders(List.of("authorization"));

    var response =
        restTemplate.exchange(
            "/signals", HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(origin, response.getHeaders().getAccessControlAllowOrigin());
  }

  @Test
  void createAsset_asTrader_returnsForbidden() {
    var body = Map.of("symbol", "TESTTRADER=X", "displayName", "Test", "category", "FOREX");
    assertEquals(
        HttpStatus.FORBIDDEN, post("/assets", token("TRADER"), body, String.class).getStatusCode());
  }

  @Test
  void listAssets_asTrader_isAuthorized() {
    var status = get("/assets", token("TRADER"), String.class).getStatusCode();
    assertNotEquals(HttpStatus.UNAUTHORIZED, status);
    assertNotEquals(HttpStatus.FORBIDDEN, status);
  }
}
