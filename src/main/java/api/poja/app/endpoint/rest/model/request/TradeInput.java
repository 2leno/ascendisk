package api.poja.app.endpoint.rest.model.request;

import api.poja.app.model.Direction;
import lombok.Builder;

@Builder
public record TradeInput(String symbol, Direction direction, Double zonePrice) {}
