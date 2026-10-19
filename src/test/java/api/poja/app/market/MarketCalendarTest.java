package api.poja.app.market;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class MarketCalendarTest {

  private final MarketCalendar calendar = new MarketCalendar(Clock.systemUTC());

  @Test
  void fridayBeforeClose_isOpen() {
    assertFalse(calendar.isOtcClosed(Instant.parse("2024-01-05T20:00:00Z")));
  }

  @Test
  void fridayAfterClose_isClosed() {
    assertTrue(calendar.isOtcClosed(Instant.parse("2024-01-05T21:30:00Z")));
  }

  @Test
  void saturday_isClosed() {
    assertTrue(calendar.isOtcClosed(Instant.parse("2024-01-06T10:00:00Z")));
  }

  @Test
  void sundayMorning_isClosed() {
    assertTrue(calendar.isOtcClosed(Instant.parse("2024-01-07T10:00:00Z")));
  }

  @Test
  void sundayAfterClose_isOpen() {
    assertFalse(calendar.isOtcClosed(Instant.parse("2024-01-07T21:30:00Z")));
  }

  @Test
  void monday_isOpen() {
    assertFalse(calendar.isOtcClosed(Instant.parse("2024-01-08T10:00:00Z")));
  }

  @Test
  void isOtcClosed_withoutArgument_usesClock() {
    var saturday =
        new MarketCalendar(Clock.fixed(Instant.parse("2024-01-06T10:00:00Z"), ZoneOffset.UTC));

    assertTrue(saturday.isOtcClosed());
  }
}
