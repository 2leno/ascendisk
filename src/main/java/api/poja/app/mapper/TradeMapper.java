package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.TradeResponse;
import api.poja.app.model.TrackedTrade;
import api.poja.app.repository.model.JTrackedTrade;
import org.springframework.stereotype.Component;

@Component
public class TradeMapper {

  public TrackedTrade toDomain(JTrackedTrade trade) {
    return TrackedTrade.builder()
        .id(trade.getId())
        .symbol(trade.getAsset().getSymbol())
        .displayName(trade.getAsset().getDisplayName())
        .direction(trade.getDirection())
        .zonePrice(trade.getZonePrice())
        .entryScore(trade.getEntryScore())
        .payout(trade.getPayout())
        .status(trade.getStatus())
        .result(trade.getResult())
        .openedAt(trade.getOpenedAt())
        .closedAt(trade.getClosedAt())
        .build();
  }

  public TradeResponse toResponse(TrackedTrade trade) {
    return TradeResponse.builder()
        .id(trade.id() == null ? null : trade.id().toString())
        .symbol(trade.symbol())
        .displayName(trade.displayName())
        .direction(trade.direction())
        .zonePrice(trade.zonePrice())
        .entryScore(trade.entryScore())
        .payout(trade.payout())
        .status(trade.status())
        .result(trade.result())
        .openedAt(trade.openedAt())
        .closedAt(trade.closedAt())
        .build();
  }
}
