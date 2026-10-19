package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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
  void manifest_isPublic() {
    var response = get("/manifest.webmanifest", null, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        MediaType.parseMediaType("application/manifest+json"),
        response.getHeaders().getContentType());
    assertTrue(response.getBody().contains("\"Ascendisk\""));
  }

  @Test
  void serviceWorker_isPublic() {
    var response = get("/sw.js", null, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("ascendisk-shell"));
  }

  @Test
  void icons_arePublic() {
    assertEquals(HttpStatus.OK, get("/img/icon-192.png", null, byte[].class).getStatusCode());
    assertEquals(HttpStatus.OK, get("/favicon.ico", null, byte[].class).getStatusCode());
  }

  @Test
  void index_isPublic() {
    var response = get("/index.html", null, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().contains("Ascendisk"));
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
