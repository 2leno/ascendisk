package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.response.CandleResponse;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.Candle;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class CandlesIT extends ApiIT {

  @MockBean MarketDataClient marketDataClient;

  @Test
  void candles_returnsRequestedCandles() {
    when(marketDataClient.candles(anyString(), anyString(), anyInt())).thenReturn(candles(10));

    var response =
        get(
            "/assets/EURUSD=X/candles?interval=5m&limit=10",
            token("TRADER"),
            CandleResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(10, response.getBody().length);
    verify(marketDataClient).candles("EURUSD=X", "5m", 10);
  }

  @Test
  void candles_arePublic() {
    when(marketDataClient.candles(anyString(), anyString(), anyInt())).thenReturn(candles(10));

    var response =
        get("/assets/EURUSD=X/candles?interval=1m&limit=10", null, CandleResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(10, response.getBody().length);
  }

  @Test
  void candles_whenMarketDataIsUnavailable_returnsServiceUnavailable() {
    when(marketDataClient.candles(anyString(), anyString(), anyInt())).thenReturn(List.of());

    var response = get("/assets/EURUSD=X/candles", null, String.class);

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
  }

  @Test
  void candles_defaultIntervalAndLimit_areUsed() {
    when(marketDataClient.candles(anyString(), anyString(), anyInt())).thenReturn(candles(60));

    var response = get("/assets/EURUSD=X/candles", token("TRADER"), CandleResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(60, response.getBody().length);
    verify(marketDataClient).candles("EURUSD=X", "1m", 60);
  }

  @Test
  void candles_unsupportedInterval_returnsBadRequest() {
    var response = get("/assets/EURUSD=X/candles?interval=7m", token("TRADER"), String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void candles_unknownAsset_returnsNotFound() {
    var response = get("/assets/NOPE=X/candles", token("TRADER"), String.class);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private List<Candle> candles(int count) {
    var candles = new ArrayList<Candle>();
    var start = Instant.parse("2024-01-03T00:00:00Z");
    for (var i = 0; i < count; i++) {
      candles.add(
          Candle.builder()
              .openTime(start.plusSeconds(60L * i))
              .open(1.1000)
              .high(1.1010)
              .low(1.0990)
              .close(1.1005)
              .volume(1.0)
              .build());
    }
    return candles;
  }
}
