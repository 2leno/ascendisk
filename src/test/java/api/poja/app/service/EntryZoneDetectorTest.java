package api.poja.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.model.Candle;
import api.poja.app.model.Direction;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntryZoneDetectorTest {

  private final EntryZoneDetector detector = new EntryZoneDetector();

  @Test
  void detect_neutralDirection_returnsCurrentPriceWithoutTouches() {
    var zone = detector.detect(List.of(candle(1.20, 1.10)), 1.15, Direction.NEUTRAL);

    assertEquals(1.15, zone.price(), 1e-9);
    assertEquals(0, zone.touches());
  }

  @Test
  void detect_emptyCandles_returnsCurrentPriceWithoutTouches() {
    var zone = detector.detect(List.of(), 1.15, Direction.BUY);

    assertEquals(1.15, zone.price(), 1e-9);
    assertEquals(0, zone.touches());
  }

  @Test
  void detect_buy_anchorsOnSupportBelowPrice() {
    var candles =
        List.of(candle(1.10, 1.05), candle(1.12, 1.05), candle(1.14, 1.06), candle(1.16, 1.06));

    var zone = detector.detect(candles, 1.16, Direction.BUY);

    assertTrue(zone.price() < 1.16);
    assertTrue(zone.touches() >= 1);
  }

  @Test
  void detect_sell_anchorsOnResistanceAbovePrice() {
    var candles = List.of(candle(1.20, 1.10), candle(1.20, 1.12), candle(1.18, 1.11));

    var zone = detector.detect(candles, 1.12, Direction.SELL);

    assertTrue(zone.price() > 1.12);
    assertTrue(zone.touches() >= 1);
  }

  private Candle candle(double high, double low) {
    var mid = (high + low) / 2;
    return Candle.builder()
        .openTime(Instant.parse("2024-01-03T00:00:00Z"))
        .open(mid)
        .high(high)
        .low(low)
        .close(mid)
        .volume(1.0)
        .build();
  }
}
