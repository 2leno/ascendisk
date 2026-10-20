package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;
import java.util.UUID;

public record LicenseKeyResponse(
    UUID id,
    String codePrefix,
    String label,
    int maxActivations,
    long activationCount,
    Instant expiresAt,
    boolean active,
    Instant createdAt) {}
