package api.poja.app.endpoint.rest.model.request;

import api.poja.app.model.TradeStatus;
import lombok.Builder;

@Builder
public record TradeUpdate(TradeStatus status, String result) {}
