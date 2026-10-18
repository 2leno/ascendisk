package api.poja.app.endpoint.rest.model.response;

import api.poja.app.model.Direction;
import java.time.Instant;
import lombok.Builder;

@Builder
public record SignalResponse(
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
