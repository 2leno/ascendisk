package api.poja.app.market;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MarketCalendar {

  private static final int WEEKEND_CLOSE_HOUR_UTC = 21;

  private final Clock clock;

  public boolean isOtcClosed() {
    return isOtcClosed(clock.instant());
  }

  public boolean isOtcClosed(Instant instant) {
    var moment = instant.atZone(ZoneOffset.UTC);
    var day = moment.getDayOfWeek();
    if (day == DayOfWeek.SATURDAY) {
      return true;
    }
    if (day == DayOfWeek.SUNDAY) {
      return moment.getHour() < WEEKEND_CLOSE_HOUR_UTC;
    }
    if (day == DayOfWeek.FRIDAY) {
      return moment.getHour() >= WEEKEND_CLOSE_HOUR_UTC;
    }
    return false;
  }
}
