package api.poja.app.endpoint.rest.model.response;

import api.poja.app.model.MemberRole;
import api.poja.app.model.MemberStatus;
import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
    UUID id,
    String email,
    String displayName,
    MemberRole role,
    MemberStatus status,
    Instant createdAt,
    Instant lastSeenAt,
    LicenseStatus license) {}
