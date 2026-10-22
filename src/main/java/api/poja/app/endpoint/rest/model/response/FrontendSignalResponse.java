package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record FrontendSignalResponse(
    long id,
    String pair,
    int score,
    Double price,
    String direction,
    Double payout,
    Double multiplier,
    boolean locked,
    boolean advice,
    Instant updatedAt) {}
