package api.poja.app.endpoint.rest.model.response;

import java.time.Instant;

public record LicenseStatus(boolean active, String codePrefix, String label, Instant expiresAt) {

  public static LicenseStatus inactive() {
    return new LicenseStatus(false, null, null, null);
  }
}
