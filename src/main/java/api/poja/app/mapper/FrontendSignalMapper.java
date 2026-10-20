package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.model.SignalAssessment;
import api.poja.app.service.SignalAccessPolicy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FrontendSignalMapper {

  private static final double DEFAULT_MULTIPLIER = 1.9;

  private final SignalAccessPolicy accessPolicy;

  public FrontendSignalResponse toResponse(SignalAssessment signal, long id) {
    return toResponse(signal, id, true);
  }

  public FrontendSignalResponse toResponse(SignalAssessment signal, long id, boolean premium) {
    var locked = accessPolicy.locked(signal, premium);
    return FrontendSignalResponse.builder()
        .id(id)
        .pair(signal.displayName())
        .score(signal.score())
        .price(signal.currentPrice())
        .direction(signal.direction().name())
        .payout(payout(signal.payout()))
        .multiplier(DEFAULT_MULTIPLIER)
        .locked(locked)
        .advice(accessPolicy.advice(signal, premium))
        .build();
  }

  private double payout(String rawPayout) {
    if (rawPayout == null || rawPayout.isBlank() || rawPayout.trim().equals("-")) {
      return 0;
    }
    try {
      return Double.parseDouble(rawPayout.replace("%", "").trim());
    } catch (NumberFormatException exception) {
      return 0;
    }
  }
}
