package api.poja.app.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IndicatorCalculator {

  public double ema(List<Double> values, int period) {
    if (values.isEmpty()) {
      return 0.0;
    }
    var multiplier = 2.0 / (period + 1);
    var ema = values.get(0);
    for (var i = 1; i < values.size(); i++) {
      ema = (values.get(i) - ema) * multiplier + ema;
    }
    return ema;
  }

  public double rsi(List<Double> values, int period) {
    if (values.size() <= period) {
      return 50.0;
    }
    var averageGain = 0.0;
    var averageLoss = 0.0;
    for (var i = 1; i <= period; i++) {
      var change = values.get(i) - values.get(i - 1);
      averageGain += change > 0 ? change : 0.0;
      averageLoss += change < 0 ? -change : 0.0;
    }
    averageGain = averageGain / period;
    averageLoss = averageLoss / period;
    for (var i = period + 1; i < values.size(); i++) {
      var change = values.get(i) - values.get(i - 1);
      var gain = change > 0 ? change : 0.0;
      var loss = change < 0 ? -change : 0.0;
      averageGain = (averageGain * (period - 1) + gain) / period;
      averageLoss = (averageLoss * (period - 1) + loss) / period;
    }
    if (averageLoss == 0.0) {
      return 100.0;
    }
    var relativeStrength = averageGain / averageLoss;
    return 100.0 - (100.0 / (1.0 + relativeStrength));
  }
}
