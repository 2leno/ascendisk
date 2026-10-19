package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.response.SignalResponse;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.Candle;
import api.poja.app.model.Direction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class SignalsIT extends ApiIT {

  private static final Instant OPEN_MARKET = Instant.parse("2024-01-03T12:00:00Z");
  private static final Instant OTC_WEEKEND = Instant.parse("2024-01-06T12:00:00Z");

  @MockBean MarketDataClient marketDataClient;

  @MockBean Clock clock;

  @Test
  void signals_onOpenMarket_returnsRisingTrend() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());

    var response = get("/signals", token("TRADER"), SignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var eurusd =
        Arrays.stream(response.getBody())
            .filter(signal -> signal.symbol().equals("EURUSD=X"))
            .findFirst()
            .orElseThrow();
    assertEquals(Direction.BUY, eurusd.trend());
    assertTrue(eurusd.ema9() > eurusd.ema21());
    assertFalse(eurusd.otcClosed());
  }

  @Test
  void signalForAsset_returnsAssessment() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());

    var response = get("/signals/EURUSD=X", token("TRADER"), SignalResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("EURUSD=X", response.getBody().symbol());
    assertEquals(Direction.BUY, response.getBody().trend());
  }

  @Test
  void signals_onOtcWeekend_areClosedAndNeutral() {
    when(clock.instant()).thenReturn(OTC_WEEKEND);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());

    var response = get("/signals", token("TRADER"), SignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(Arrays.stream(response.getBody()).allMatch(SignalResponse::otcClosed));
    assertTrue(
        Arrays.stream(response.getBody())
            .allMatch(signal -> signal.direction() == Direction.NEUTRAL));
  }

  @Test
  void signals_withoutMarketData_areNeutral() {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(List.of());

    var response = get("/signals", token("TRADER"), SignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(
        Arrays.stream(response.getBody())
            .allMatch(signal -> signal.direction() == Direction.NEUTRAL));
  }

  @Test
  void signalFor_unknownAsset_returnsNotFound() {
    var response = get("/signals/NOPE=X", token("TRADER"), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void stream_pushesSignalEvents() throws Exception {
    when(clock.instant()).thenReturn(OPEN_MARKET);
    when(marketDataClient.minuteCandles(anyString(), anyInt())).thenReturn(risingCandles());
    var client = HttpClient.newHttpClient();
    var request =
        HttpRequest.newBuilder(URI.create(restTemplate.getRootUri() + "/signals/stream"))
            .header("Authorization", "Bearer " + token("TRADER"))
            .header("Accept", "text/event-stream")
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    try (var reader =
        new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
      var payload = firstEventData(reader);
      assertTrue(payload.contains("EURUSD=X"));
      assertTrue(payload.contains("trend"));
    }
  }

  private String firstEventData(BufferedReader reader) throws IOException {
    var data = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("data:")) {
        data.append(line.substring("data:".length()).trim());
        break;
      }
    }
    return data.toString();
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
