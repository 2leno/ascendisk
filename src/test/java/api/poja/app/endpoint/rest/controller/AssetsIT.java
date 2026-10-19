package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.endpoint.rest.model.response.AssetResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AssetsIT extends ApiIT {

  @Test
  void listAssets_returnsSeededAssets() {
    var response = get("/assets", token("ADMIN"), AssetResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var symbols = Arrays.stream(response.getBody()).map(AssetResponse::symbol).toList();
    assertTrue(symbols.contains("EURUSD=X"));
    assertTrue(symbols.contains("GBPUSD=X"));
  }

  @Test
  void listAssets_enabledOnly_returnsEnabledAssets() {
    var response = get("/assets?enabled=true", token("TRADER"), AssetResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 8);
    assertTrue(Arrays.stream(response.getBody()).allMatch(AssetResponse::enabled));
  }

  @Test
  void createAsset_asAdmin_creates() {
    var symbol = "TEST" + UUID.randomUUID().toString().substring(0, 8) + "=X";
    var body =
        Map.of(
            "symbol", symbol,
            "displayName", "Test Pair",
            "category", "FOREX",
            "payout", "80%",
            "enabled", true);

    var response = post("/assets", token("ADMIN"), body, AssetResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(symbol, response.getBody().symbol());
    assertEquals("Test Pair", response.getBody().displayName());
    assertEquals("80%", response.getBody().payout());
  }

  @Test
  void createAsset_duplicateSymbol_returnsConflict() {
    var body = Map.of("symbol", "EURUSD=X", "displayName", "Duplicate", "category", "FOREX");

    var response = post("/assets", token("ADMIN"), body, String.class);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  void createAsset_blankSymbol_returnsBadRequest() {
    var body = Map.of("symbol", " ", "displayName", "Blank", "category", "FOREX");

    var response = post("/assets", token("ADMIN"), body, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void updateAsset_asAdmin_updates() {
    var body =
        Map.of(
            "symbol", "USDJPY=X",
            "displayName", "USD/JPY",
            "category", "FOREX",
            "payout", "91%",
            "enabled", true);

    var response = put("/assets/USDJPY=X", token("ADMIN"), body, AssetResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("91%", response.getBody().payout());
  }

  @Test
  void updateAsset_unknownSymbol_returnsNotFound() {
    var body = Map.of("symbol", "NOPE=X", "displayName", "Nope", "category", "FOREX");

    var response = put("/assets/NOPE=X", token("ADMIN"), body, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
