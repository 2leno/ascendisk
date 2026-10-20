package api.poja.app.service;

import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LicenseService {

  private static final String EMPTY_KEY_MESSAGE = "Entre ta clé d'activation.";
  private static final String NO_ACTIVE_KEY_MESSAGE = "Aucune clé active pour le moment.";
  private static final String INVALID_KEY_MESSAGE = "Clé invalide.";
  private static final String ACTIVATED_KEY_MESSAGE = "Clé activée. Bon trading !";

  private final Set<String> keys;

  public LicenseService(@Value("${app.license.keys:}") String configuredKeys) {
    this.keys =
        Arrays.stream(configuredKeys.split(","))
            .map(String::trim)
            .filter(key -> !key.isEmpty())
            .map(String::toUpperCase)
            .collect(Collectors.toUnmodifiableSet());
  }

  public LicenseActivationResponse activate(String key) {
    if (key == null || key.isBlank()) {
      return new LicenseActivationResponse(false, EMPTY_KEY_MESSAGE);
    }
    if (keys.isEmpty()) {
      return new LicenseActivationResponse(false, NO_ACTIVE_KEY_MESSAGE);
    }
    if (keys.contains(key.trim().toUpperCase())) {
      return new LicenseActivationResponse(true, ACTIVATED_KEY_MESSAGE);
    }
    return new LicenseActivationResponse(false, INVALID_KEY_MESSAGE);
  }
}
