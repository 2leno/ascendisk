package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;
import java.util.UUID;

public record LicenseActivationItem(
    UUID id, UUID memberId, String email, Instant activatedAt, Instant revokedAt) {}
