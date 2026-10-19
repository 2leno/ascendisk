package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PingIT extends ApiIT {

  @Test
  void ping_withoutToken_returnsPong() {
    var response = get("/ping", null, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("pong", response.getBody());
  }
}
