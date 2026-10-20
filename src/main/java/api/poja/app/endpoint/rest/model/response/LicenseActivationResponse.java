package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;

public record LicenseActivationResponse(boolean success, String message, Instant expiresAt) {}
