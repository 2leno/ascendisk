package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record AccountResponse(
    String username, double balance, String currency, Instant updatedAt) {}
