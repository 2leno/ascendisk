package api.poja.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import api.poja.app.model.Asset;
import api.poja.app.model.AssetCategory;
import api.poja.app.model.Candle;
import api.poja.app.model.Direction;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SignalScoringServiceTest {

  private static final Instant NOW = Instant.parse("2024-01-03T12:00:00Z");
  private static final Asset ASSET =
      Asset.builder()
          .symbol("EURUSD=X")
          .displayName("EUR/USD")
          .category(AssetCategory.FOREX)
          .payout("-")
          .enabled(true)
          .build();

  private final IndicatorCalculator indicatorCalculator = mock(IndicatorCalculator.class);
  private final EntryZoneDetector entryZoneDetector = mock(EntryZoneDetector.class);
  private final SignalScoringService service =
      new SignalScoringService(indicatorCalculator, entryZoneDetector);

  @Test
  void assess_bullishTrendWithOversoldRsi_scoresEightyAndIsTradable() {
    stub(1.12, 1.10, 40.0, 1.10, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(Direction.BUY, result.trend());
    assertEquals(40, result.momentum());
    assertEquals(80, result.score());
    assertEquals(Direction.BUY, result.direction());
    assertTrue(result.tradable());
  }

  @Test
  void assess_bullishTrendWithNeutralRsi_scalesMomentumFromRsi() {
    stub(1.12, 1.10, 50.0, 1.10, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(25, result.momentum());
    assertEquals(65, result.score());
    assertEquals(Direction.NEUTRAL, result.direction());
    assertFalse(result.tradable());
  }

  @Test
  void assess_bullishTrendNearCap_keepsScoreBelowEighty() {
    stub(1.12, 1.10, 58.0, 1.10, 4);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(17, result.momentum());
    assertEquals(20, result.zoneStrength());
    assertEquals(77, result.score());
    assertEquals(Direction.BUY, result.direction());
  }

  @Test
  void assess_bullishTrendWithOverboughtRsi_addsNoMomentum() {
    stub(1.12, 1.10, 70.0, 1.10, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(0, result.momentum());
    assertEquals(40, result.score());
  }

  @Test
  void assess_bearishTrendWithOverboughtRsi_scoresEighty() {
    stub(1.10, 1.12, 65.0, 1.20, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(Direction.SELL, result.trend());
    assertEquals(40, result.momentum());
    assertEquals(80, result.score());
    assertEquals(Direction.SELL, result.direction());
  }

  @Test
  void assess_bearishTrendWithNeutralRsi_addsTwentyMomentum() {
    stub(1.10, 1.12, 50.0, 1.20, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(20, result.momentum());
  }

  @Test
  void assess_bearishTrendWithWeakMomentum_keepsScoreTradable() {
    stub(1.10, 1.12, 42.0, 1.20, 4);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(15, result.momentum());
    assertEquals(20, result.zoneStrength());
    assertEquals(75, result.score());
    assertEquals(Direction.SELL, result.direction());
    assertTrue(result.tradable());
  }

  @Test
  void assess_bearishTrendWithOversoldRsi_addsNoMomentum() {
    stub(1.10, 1.12, 30.0, 1.20, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(0, result.momentum());
  }

  @Test
  void assess_zoneTouches_addUpToTwentyPoints() {
    stub(1.12, 1.10, 40.0, 1.10, 10);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(20, result.zoneStrength());
    assertEquals(100, result.score());
  }

  @Test
  void assess_otcClosed_forcesNeutralAndNotTradable() {
    stub(1.12, 1.10, 40.0, 1.10, 0);

    var result = service.assess(ASSET, candles(), true, NOW);

    assertEquals(Direction.NEUTRAL, result.direction());
    assertFalse(result.tradable());
    assertTrue(result.otcClosed());
  }

  @Test
  void assess_neutralTrend_scoresZero() {
    stub(1.10, 1.10, 50.0, 1.10, 0);

    var result = service.assess(ASSET, candles(), false, NOW);

    assertEquals(Direction.NEUTRAL, result.trend());
    assertEquals(0, result.score());
    assertEquals(Direction.NEUTRAL, result.direction());
  }

  @Test
  void assess_emptyCandles_returnsEmptyAssessment() {
    var result = service.assess(ASSET, List.of(), false, NOW);

    assertEquals("EURUSD=X", result.symbol());
    assertEquals(Direction.NEUTRAL, result.direction());
    assertEquals(0, result.score());
    assertFalse(result.tradable());
    assertEquals("-", result.payout());
  }

  private void stub(double ema9, double ema21, double rsi, double zonePrice, int touches) {
    when(indicatorCalculator.ema(anyList(), eq(9))).thenReturn(ema9);
    when(indicatorCalculator.ema(anyList(), eq(21))).thenReturn(ema21);
    when(indicatorCalculator.rsi(anyList(), eq(14))).thenReturn(rsi);
    when(entryZoneDetector.detect(anyList(), anyDouble(), any()))
        .thenReturn(new EntryZone(zonePrice, touches));
  }

  private List<Candle> candles() {
    return List.of(
        Candle.builder()
            .openTime(NOW)
            .open(1.10)
            .high(1.11)
            .low(1.09)
            .close(1.10)
            .volume(1.0)
            .build());
  }
}
