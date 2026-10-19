package api.poja.app.service;

import api.poja.app.model.Asset;
import api.poja.app.model.Candle;
import api.poja.app.model.Direction;
import api.poja.app.model.SignalAssessment;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SignalScoringService {

  private static final int EMA_FAST = 9;
  private static final int EMA_SLOW = 21;
  private static final int RSI_PERIOD = 14;
  private static final int TREND_SCORE = 40;
  private static final int MOMENTUM_STRONG = 40;
  private static final int MOMENTUM_NEUTRAL = 20;
  private static final int ZONE_PER_TOUCH = 5;
  private static final int ZONE_MAX = 20;
  private static final int TRADE_THRESHOLD = 75;
  private static final double RSI_BULLISH = 55.0;
  private static final double RSI_BEARISH = 45.0;

  private final IndicatorCalculator indicatorCalculator;
  private final EntryZoneDetector entryZoneDetector;

  public SignalAssessment assess(
      Asset asset, List<Candle> candles, boolean otcClosed, Instant evaluatedAt) {
    if (candles.isEmpty()) {
      return emptyAssessment(asset, otcClosed, evaluatedAt);
    }
    var closes = candles.stream().map(Candle::close).toList();
    var current = candles.get(candles.size() - 1);
    var ema9 = indicatorCalculator.ema(closes, EMA_FAST);
    var ema21 = indicatorCalculator.ema(closes, EMA_SLOW);
    var rsi = indicatorCalculator.rsi(closes, RSI_PERIOD);

    var trend = ema9 > ema21 ? Direction.BUY : ema9 < ema21 ? Direction.SELL : Direction.NEUTRAL;
    var momentum = momentum(trend, rsi);
    var zone = entryZoneDetector.detect(candles, current.close(), trend);
    var zoneStrength = Math.min(ZONE_MAX, zone.touches() * ZONE_PER_TOUCH);
    var trendScore = trend == Direction.NEUTRAL ? 0 : TREND_SCORE;
    var score = Math.min(100, trendScore + momentum + zoneStrength);
    var direction =
        !otcClosed && score >= TRADE_THRESHOLD && trend != Direction.NEUTRAL
            ? trend
            : Direction.NEUTRAL;

    return SignalAssessment.builder()
        .symbol(asset.symbol())
        .displayName(asset.displayName())
        .currentPrice(current.close())
        .openPrice(current.open())
        .ema9(ema9)
        .ema21(ema21)
        .rsi(rsi)
        .trend(trend)
        .momentum(momentum)
        .zonePrice(zone.price())
        .zoneTouches(zone.touches())
        .zoneStrength(zoneStrength)
        .score(score)
        .direction(direction)
        .tradable(direction != Direction.NEUTRAL && !otcClosed)
        .distance(current.close() - zone.price())
        .payout(payout(asset))
        .otcClosed(otcClosed)
        .evaluatedAt(evaluatedAt)
        .build();
  }

  private SignalAssessment emptyAssessment(Asset asset, boolean otcClosed, Instant evaluatedAt) {
    return SignalAssessment.builder()
        .symbol(asset.symbol())
        .displayName(asset.displayName())
        .trend(Direction.NEUTRAL)
        .direction(Direction.NEUTRAL)
        .payout(payout(asset))
        .otcClosed(otcClosed)
        .evaluatedAt(evaluatedAt)
        .build();
  }

  private int momentum(Direction trend, double rsi) {
    if (trend == Direction.BUY) {
      return rsi >= RSI_BULLISH ? MOMENTUM_STRONG : rsi > RSI_BEARISH ? MOMENTUM_NEUTRAL : 0;
    }
    if (trend == Direction.SELL) {
      return rsi <= RSI_BEARISH ? MOMENTUM_STRONG : rsi < RSI_BULLISH ? MOMENTUM_NEUTRAL : 0;
    }
    return 0;
  }

  private String payout(Asset asset) {
    return asset.payout() == null || asset.payout().isBlank() ? "-" : asset.payout();
  }
}
