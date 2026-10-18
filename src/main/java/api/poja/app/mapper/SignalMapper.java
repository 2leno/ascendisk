package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.SignalResponse;
import api.poja.app.model.SignalAssessment;
import org.springframework.stereotype.Component;

@Component
public class SignalMapper {

  public SignalResponse toResponse(SignalAssessment signal) {
    return SignalResponse.builder()
        .symbol(signal.symbol())
        .displayName(signal.displayName())
        .currentPrice(signal.currentPrice())
        .openPrice(signal.openPrice())
        .ema9(signal.ema9())
        .ema21(signal.ema21())
        .rsi(signal.rsi())
        .trend(signal.trend())
        .momentum(signal.momentum())
        .zonePrice(signal.zonePrice())
        .zoneTouches(signal.zoneTouches())
        .zoneStrength(signal.zoneStrength())
        .score(signal.score())
        .direction(signal.direction())
        .tradable(signal.tradable())
        .distance(signal.distance())
        .payout(signal.payout())
        .otcClosed(signal.otcClosed())
        .evaluatedAt(signal.evaluatedAt())
        .build();
  }
}
