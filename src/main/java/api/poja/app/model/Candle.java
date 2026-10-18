package api.poja.app.model;

import java.time.Instant;
import lombok.Builder;

@Builder
public record Candle(
    Instant openTime, double open, double high, double low, double close, double volume) {}
