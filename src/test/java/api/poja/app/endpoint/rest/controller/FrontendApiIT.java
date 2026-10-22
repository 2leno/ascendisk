package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.Candle;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class FrontendApiIT extends ApiIT {

  private static final Instant OPEN_MARKET = Instant.parse("2024-01-03T12:00:00Z");

  @MockBean MarketDataClient marketDataClient;

  @MockBean Clock clock;

  @BeforeEach
  void fixedClock() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
  }

  @Test
  void signals_arePublicAndFollowFrontendContract() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());

    var response = get("/api/signals", adminFirebaseToken(), FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var eurusd =
        Arrays.stream(response.getBody())
            .filter(signal -> signal.pair().equals("EUR/USD"))
            .findFirst()
            .orElseThrow();
    assertTrue(eurusd.id() > 0);
    assertTrue(eurusd.score() >= 0);
    assertNotNull(eurusd.updatedAt());
    assertTrue(eurusd.locked() != eurusd.advice());
    if (eurusd.locked()) {
      assertNull(eurusd.price());
      assertNull(eurusd.direction());
      assertNull(eurusd.payout());
      assertNull(eurusd.multiplier());
    } else {
      assertTrue(eurusd.price() > 0);
      assertTrue(List.of("BUY", "SELL", "NEUTRAL").contains(eurusd.direction()));
      assertEquals(1.9, eurusd.multiplier());
    }
  }

  @Test
  void lockedSignals_doNotExposePremiumFields() {
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());

    var response = get("/api/signals", null, FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(
        Arrays.stream(response.getBody())
            .filter(FrontendSignalResponse::locked)
            .allMatch(
                signal ->
                    signal.price() == null
                        && signal.direction() == null
                        && signal.payout() == null
                        && signal.multiplier() == null));
  }

  @Test
  void signals_withoutMarketData_returnNoTradableSignalsForFreeUsers() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(List.of());

    var response = get("/api/signals", null, FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().length);
  }

  @Test
  void signals_withoutMarketData_areVisibleToAdmins() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(List.of());

    var response = get("/api/signals", adminFirebaseToken(), FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(Arrays.stream(response.getBody()).allMatch(FrontendSignalResponse::locked));
    assertTrue(Arrays.stream(response.getBody()).allMatch(signal -> signal.score() == 0));
    assertTrue(Arrays.stream(response.getBody()).allMatch(signal -> signal.direction() == null));
  }

  @Test
  void activate_withoutToken_returnsUnauthorized() {
    var response =
        post(
            "/api/licenses/activate",
            null,
            Map.of("key", "OBK-UNKNOWN"),
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void activate_withoutBody_returnsFailure() {
    var response =
        post(
            "/api/licenses/activate",
            firebaseToken("buyer@test.local"),
            null,
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().success());
    assertFalse(response.getBody().message().isBlank());
  }

  @Test
  void activate_withoutKey_returnsFailure() {
    var response =
        post(
            "/api/licenses/activate",
            firebaseToken("buyer@test.local"),
            Map.of(),
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().success());
    assertFalse(response.getBody().message().isBlank());
  }

  @Test
  void activate_unknownKey_returnsFailure() {
    var response =
        post(
            "/api/licenses/activate",
            firebaseToken("buyer@test.local"),
            Map.of("key", "OBK-UNKNOWN"),
            LicenseActivationResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().success());
  }

  private List<Candle> risingCandles() {
    var candles = new ArrayList<Candle>();
    var start = Instant.parse("2024-01-03T00:00:00Z");
    var price = 1.1000;
    for (var i = 0; i < 60; i++) {
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
