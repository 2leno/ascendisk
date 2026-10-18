package api.poja.app.endpoint.rest.model.response;

import api.poja.app.model.Direction;
import api.poja.app.model.TradeStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record TradeResponse(
    String id,
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
