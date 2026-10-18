package api.poja.app.service;

import api.poja.app.model.Candle;
import api.poja.app.model.Direction;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EntryZoneDetector {

  private static final double TOLERANCE_RATIO = 0.0005;

  public EntryZone detect(List<Candle> candles, double currentPrice, Direction direction) {
    if (candles.isEmpty() || direction == Direction.NEUTRAL) {
      return new EntryZone(currentPrice, 0);
    }
    var tolerance = currentPrice * TOLERANCE_RATIO;
    var levels = new ArrayList<Double>();
    for (var candle : candles) {
      levels.add(direction == Direction.BUY ? candle.low() : candle.high());
    }
    levels.sort(Double::compare);
    var best = new EntryZone(currentPrice, 0);
    var hasBest = false;
    var index = 0;
    while (index < levels.size()) {
      var anchor = levels.get(index);
      var sum = anchor;
      var next = index + 1;
      while (next < levels.size() && levels.get(next) - anchor <= tolerance) {
        sum += levels.get(next);
        next++;
      }
      var level = sum / (next - index);
      var candidate = new EntryZone(level, countTouches(candles, level, tolerance, direction));
      if (isFavorable(direction, level, currentPrice, tolerance)
          && (!hasBest || better(candidate, best, currentPrice))) {
        best = candidate;
        hasBest = true;
      }
      index = next;
    }
    return best;
  }

  private int countTouches(
      List<Candle> candles, double level, double tolerance, Direction direction) {
    var touches = 0;
    for (var candle : candles) {
      var extreme = direction == Direction.BUY ? candle.low() : candle.high();
      if (Math.abs(extreme - level) <= tolerance) {
        touches++;
      }
    }
    return touches;
  }

  private boolean isFavorable(
      Direction direction, double level, double currentPrice, double tolerance) {
    if (direction == Direction.BUY) {
      return level <= currentPrice + tolerance;
    }
    return level >= currentPrice - tolerance;
  }

  private boolean better(EntryZone candidate, EntryZone current, double currentPrice) {
    if (candidate.touches() != current.touches()) {
      return candidate.touches() > current.touches();
    }
    return Math.abs(candidate.price() - currentPrice) < Math.abs(current.price() - currentPrice);
  }
}
