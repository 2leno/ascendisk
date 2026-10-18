package api.poja.app.model;

import java.time.Instant;
import lombok.Builder;

@Builder
public record SignalAssessment(
    String symbol,
    String displayName,
    double currentPrice,
    double openPrice,
    double ema9,
    double ema21,
    double rsi,
    Direction trend,
    int momentum,
    double zonePrice,
    int zoneTouches,
    int zoneStrength,
    int score,
    Direction direction,
    boolean tradable,
    double distance,
    String payout,
    boolean otcClosed,
    Instant evaluatedAt) {}
