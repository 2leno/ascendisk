package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.model.SignalAssessment;
import org.springframework.stereotype.Component;

@Component
public class FrontendSignalMapper {

  private static final double DEFAULT_MULTIPLIER = 1.9;

  public FrontendSignalResponse toResponse(SignalAssessment signal, long id) {
    return FrontendSignalResponse.builder()
        .id(id)
        .pair(signal.displayName())
        .score(signal.score())
        .price(signal.currentPrice())
        .direction(signal.direction().name())
        .payout(payout(signal.payout()))
        .multiplier(DEFAULT_MULTIPLIER)
        .locked(!signal.tradable())
        .advice(signal.tradable())
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
