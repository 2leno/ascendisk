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
  private static final double RSI_OVERSOLD = 45.0;
  private static final double RSI_OVERBOUGHT = 55.0;
  private static final double RSI_BULLISH_CAP = 60.0;
  private static final double RSI_BEARISH_FLOOR = 40.0;
  private static final double MOMENTUM_SLOPE = 1.0;
  private static final double BUY_RSI_MIDPOINT = 55.0;
  private static final double SELL_RSI_MIDPOINT = 50.0;

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
      if (rsi < RSI_OVERSOLD) {
        return MOMENTUM_STRONG;
      }
      if (rsi < RSI_BULLISH_CAP) {
        return clampMomentum(
            MOMENTUM_NEUTRAL + (int) Math.round((BUY_RSI_MIDPOINT - rsi) * MOMENTUM_SLOPE));
      }
      return 0;
    }
    if (trend == Direction.SELL) {
      if (rsi > RSI_OVERBOUGHT) {
        return MOMENTUM_STRONG;
      }
      if (rsi > RSI_BEARISH_FLOOR) {
        return clampMomentum(
            MOMENTUM_NEUTRAL + (int) Math.round((rsi - SELL_RSI_MIDPOINT) * MOMENTUM_SLOPE));
      }
    }
    return 0;
  }

  private int clampMomentum(int value) {
    return Math.max(0, Math.min(MOMENTUM_STRONG, value));
  }

  private String payout(Asset asset) {
    return asset.payout() == null || asset.payout().isBlank() ? "-" : asset.payout();
  }
}
