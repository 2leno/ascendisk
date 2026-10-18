package api.poja.app.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Account(
    UUID id, String username, double balance, String currency, Instant updatedAt) {}
