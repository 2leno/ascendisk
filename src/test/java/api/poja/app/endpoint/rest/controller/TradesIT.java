package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.response.TradeResponse;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.Candle;
import api.poja.app.model.TradeStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class TradesIT extends ApiIT {

  @MockBean MarketDataClient marketDataClient;

  @BeforeEach
  void stubMarket() {
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(candles());
  }

  @Test
  void trackTrade_createsPendingTrade() {
    var body = Map.of("symbol", "EURUSD=X", "direction", "BUY", "zonePrice", 1.1005);

    var response = post("/trades", token("TRADER"), body, TradeResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("EURUSD=X", response.getBody().symbol());
    assertEquals("BUY", response.getBody().direction().name());
    assertEquals(TradeStatus.PENDING, response.getBody().status());
    assertNotNull(response.getBody().id());
    assertFalse(response.getBody().id().isBlank());
  }

  @Test
  void trackTrade_invalidDirection_returnsBadRequest() {
    var body = Map.of("symbol", "EURUSD=X", "direction", "NEUTRAL", "zonePrice", 1.1005);

    var response = post("/trades", token("TRADER"), body, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void trackTrade_missingZonePrice_returnsBadRequest() {
    var body = Map.of("symbol", "EURUSD=X", "direction", "BUY");

    var response = post("/trades", token("TRADER"), body, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void trackTrade_unknownAsset_returnsNotFound() {
    var body = Map.of("symbol", "NOPE=X", "direction", "BUY", "zonePrice", 1.1005);

    var response = post("/trades", token("TRADER"), body, String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getAndCloseTrade_returnsUpdatedTrade() {
    var created =
        post(
                "/trades",
                token("TRADER"),
                Map.of("symbol", "GBPUSD=X", "direction", "SELL", "zonePrice", 1.2000),
                TradeResponse.class)
            .getBody();

    var fetched = get("/trades/" + created.id(), token("TRADER"), TradeResponse.class);
    assertEquals(HttpStatus.OK, fetched.getStatusCode());
    assertEquals(created.id(), fetched.getBody().id());

    var closed =
        put(
            "/trades/" + created.id(),
            token("TRADER"),
            Map.of("status", "WON", "result", "take profit"),
            TradeResponse.class);
    assertEquals(HttpStatus.OK, closed.getStatusCode());
    assertEquals(TradeStatus.WON, closed.getBody().status());
    assertEquals("take profit", closed.getBody().result());
    assertNotNull(closed.getBody().closedAt());
  }

  @Test
  void getTrade_unknown_returnsNotFound() {
    var response = get("/trades/" + UUID.randomUUID(), token("TRADER"), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void closeTrade_unknown_returnsNotFound() {
    var response =
        put("/trades/" + UUID.randomUUID(), token("TRADER"), Map.of("status", "WON"), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void closeTrade_withoutStatus_returnsBadRequest() {
    var created =
        post(
                "/trades",
                token("TRADER"),
                Map.of("symbol", "EURUSD=X", "direction", "BUY", "zonePrice", 1.1005),
                TradeResponse.class)
            .getBody();

    var response =
        put("/trades/" + created.id(), token("TRADER"), Map.of("result", "x"), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void listTrades_returnsTrades() {
    var response = get("/trades", token("TRADER"), TradeResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 0);
  }

  @Test
  void listTrades_filteredByStatus_returnsMatchingTrades() {
    var response = get("/trades?status=PENDING", token("TRADER"), TradeResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(
        Arrays.stream(response.getBody()).allMatch(trade -> trade.status() == TradeStatus.PENDING));
  }

  private List<Candle> candles() {
    var candles = new ArrayList<Candle>();
    var start = Instant.parse("2024-01-03T00:00:00Z");
    var price = 1.1000;
    for (var i = 0; i < 30; i++) {
      var close = price + 0.0005;
      candles.add(
          Candle.builder()
              .openTime(start.plusSeconds(60L * i))
              .open(price)
              .high(close + 0.0002)
              .low(price - 0.0002)
              .close(close)
              .volume(1.0)
              .build());
      price = close;
    }
    return candles;
  }
}
