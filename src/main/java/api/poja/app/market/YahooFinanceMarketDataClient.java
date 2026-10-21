package api.poja.app.market;

import api.poja.app.model.Candle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class YahooFinanceMarketDataClient implements MarketDataClient {

  private static final String ONE_MINUTE_INTERVAL = "1m";
  private static final String ONE_DAY_RANGE = "1d";

  private final String baseUrl;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public YahooFinanceMarketDataClient(
      @Value("${app.market.yahoo.base-url}") String baseUrl, ObjectMapper objectMapper) {
    this.baseUrl = baseUrl;
    this.objectMapper = objectMapper;
    this.restClient =
        RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; ascendisk/1.0)")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();
  }

  @Override
  public List<Candle> minuteCandles(String symbol, int limit) {
    return candles(symbol, ONE_MINUTE_INTERVAL, limit);
  }

  @Override
  public List<Candle> candles(String symbol, String interval, int limit) {
    try {
      var url =
          baseUrl
              + "/v8/finance/chart/"
              + symbol
              + "?interval="
              + interval
              + "&range="
              + ONE_DAY_RANGE;
      var body = restClient.get().uri(URI.create(url)).retrieve().body(String.class);
      return parse(symbol, body, limit);
    } catch (Exception e) {
      log.warn("Yahoo Finance unreachable for {}: {}", symbol, e.getMessage());
      return List.of();
    }
  }

  private List<Candle> parse(String symbol, String body, int limit) {
    if (body == null) {
      return List.of();
    }
    var root = readTree(body);
    var result = root.path("chart").path("result");
    if (!result.isArray() || result.isEmpty()) {
      log.warn("Yahoo Finance returned no result for {}", symbol);
      return List.of();
    }
    var node = result.get(0);
    var timestamps = node.path("timestamp");
    var quote = node.path("indicators").path("quote").path(0);
    var opens = quote.path("open");
    var highs = quote.path("high");
    var lows = quote.path("low");
    var closes = quote.path("close");
    var volumes = quote.path("volume");

    var candles = new ArrayList<Candle>();
    for (int i = 0; i < timestamps.size(); i++) {
      if (opens.path(i).isNull()
          || highs.path(i).isNull()
          || lows.path(i).isNull()
          || closes.path(i).isNull()) {
        continue;
      }
      candles.add(
          Candle.builder()
              .openTime(Instant.ofEpochSecond(timestamps.path(i).asLong()))
              .open(opens.path(i).asDouble())
              .high(highs.path(i).asDouble())
              .low(lows.path(i).asDouble())
              .close(closes.path(i).asDouble())
              .volume(volumes.path(i).isNull() ? 0.0 : volumes.path(i).asDouble())
              .build());
    }
    if (candles.size() > limit) {
      return List.copyOf(candles.subList(candles.size() - limit, candles.size()));
    }
    return List.copyOf(candles);
  }

  @SneakyThrows
  private JsonNode readTree(String body) {
    return objectMapper.readTree(body);
  }
}
