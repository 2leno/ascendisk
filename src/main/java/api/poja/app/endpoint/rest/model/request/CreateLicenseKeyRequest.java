package api.poja.app.endpoint.rest.model.request;

import java.time.Instant;

public record CreateLicenseKeyRequest(String label, Integer maxActivations, Instant expiresAt) {}
