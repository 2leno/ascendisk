package api.poja.app.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.model.Direction;
import api.poja.app.model.SignalAssessment;
import org.junit.jupiter.api.Test;

class FrontendSignalMapperTest {

  private final FrontendSignalMapper mapper = new FrontendSignalMapper();

  @Test
  void toResponse_mapsTradableSignalToUnlockedCard() {
    var response = mapper.toResponse(signal(82, Direction.BUY, true, "34%"), 3);

    assertEquals(3, response.id());
    assertEquals("EUR/USD", response.pair());
    assertEquals(82, response.score());
    assertEquals(1.1, response.price());
    assertEquals("BUY", response.direction());
    assertEquals(34, response.payout());
    assertEquals(1.9, response.multiplier());
    assertFalse(response.locked());
    assertTrue(response.advice());
  }

  @Test
  void toResponse_mapsNonTradableSignalToLockedCard() {
    var response = mapper.toResponse(signal(0, Direction.NEUTRAL, false, "-"), 1);

    assertEquals(0, response.payout());
    assertTrue(response.locked());
    assertFalse(response.advice());
  }

  @Test
  void toResponse_withInvalidPayout_usesZero() {
    var response = mapper.toResponse(signal(50, Direction.SELL, false, "n/a"), 2);

    assertEquals(0, response.payout());
  }

  private SignalAssessment signal(int score, Direction direction, boolean tradable, String payout) {
    return SignalAssessment.builder()
        .symbol("EURUSD=X")
        .displayName("EUR/USD")
        .currentPrice(1.1)
        .score(score)
        .direction(direction)
        .tradable(tradable)
        .payout(payout)
        .build();
  }
}
