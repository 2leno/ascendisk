package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthIT extends ApiIT {

  @Test
  void login_asAdmin_returnsTokenAndRole() {
    var response = login("admin", "admin123");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("ADMIN", response.getBody().get("role"));
    var token = (String) response.getBody().get("token");
    assertNotNull(token);
    assertFalse(token.isBlank());
    assertEquals("admin", jwtTokenProvider.getUsername(token));
    assertEquals(List.of("ADMIN"), jwtTokenProvider.getRoles(token));
  }

  @Test
  void login_asTrader_returnsTokenAndRole() {
    var response = login("trader", "trader123");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("TRADER", response.getBody().get("role"));
  }

  @Test
  void login_wrongPassword_returnsUnauthorized() {
    assertEquals(HttpStatus.UNAUTHORIZED, login("admin", "wrong-password").getStatusCode());
  }

  @Test
  void login_unknownUser_returnsUnauthorized() {
    assertEquals(HttpStatus.UNAUTHORIZED, login("ghost", "password").getStatusCode());
  }

  @SuppressWarnings("rawtypes")
  private ResponseEntity<Map> login(String username, String password) {
    return post("/auth/login", null, Map.of("username", username, "password", password), Map.class);
  }
}
