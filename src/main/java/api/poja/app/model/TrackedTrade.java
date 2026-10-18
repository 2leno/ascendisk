package api.poja.app.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TrackedTrade(
    UUID id,
    String symbol,
    String displayName,
    Direction direction,
    double zonePrice,
    int entryScore,
    String payout,
    TradeStatus status,
    String result,
    Instant openedAt,
    Instant closedAt) {}
