package api.poja.app.service;

import api.poja.app.model.SignalAssessment;
import org.springframework.stereotype.Component;

@Component
public class SignalAccessPolicy {

  public static final int FREE_MIN_SCORE = 75;
  public static final int PREMIUM_MIN_SCORE = 80;

  public boolean visible(SignalAssessment signal, boolean premium) {
    if (premium) {
      return true;
    }
    return signal.score() >= PREMIUM_MIN_SCORE
        || (signal.tradable() && signal.score() >= FREE_MIN_SCORE);
  }

  public boolean locked(SignalAssessment signal, boolean premium) {
    if (premium) {
      return !signal.tradable();
    }
    return signal.score() >= PREMIUM_MIN_SCORE;
  }

  public boolean advice(SignalAssessment signal, boolean premium) {
    return signal.tradable() && !locked(signal, premium);
  }
}
