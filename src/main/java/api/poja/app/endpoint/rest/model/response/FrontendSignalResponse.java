package api.poja.app.endpoint.rest.model.response;

import lombok.Builder;

@Builder
public record FrontendSignalResponse(
    long id,
    String pair,
    int score,
    double price,
    String direction,
    double payout,
    double multiplier,
    boolean locked,
    boolean advice) {}
