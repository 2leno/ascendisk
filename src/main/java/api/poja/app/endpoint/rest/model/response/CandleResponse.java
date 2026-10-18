package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record CandleResponse(
    Instant openTime, double open, double high, double low, double close, double volume) {}
