package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import api.poja.app.endpoint.rest.model.response.AccountResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AccountIT extends ApiIT {

  @Test
  void getAccount_asAdmin_returnsAccount() {
    var response = get("/account", token("admin", "ADMIN"), AccountResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("admin", response.getBody().username());
    assertEquals("USD", response.getBody().currency());
  }

  @Test
  void getAccount_asTrader_returnsAccount() {
    var response = get("/account", token("trader", "TRADER"), AccountResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("trader", response.getBody().username());
  }

  @Test
  void updateAccount_balance_isApplied() {
    var response =
        put(
            "/account",
            token("trader", "TRADER"),
            Map.of("balance", 2500.0),
            AccountResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2500.0, response.getBody().balance(), 0.001);
  }

  @Test
  void updateAccount_negativeBalance_returnsBadRequest() {
    var response =
        put("/account", token("trader", "TRADER"), Map.of("balance", -5.0), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateAccount_missingBalance_returnsBadRequest() {
    var response = put("/account", token("trader", "TRADER"), Map.of(), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void getAccount_unknownUser_returnsNotFound() {
    var response = get("/account", token("ghost", "TRADER"), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
