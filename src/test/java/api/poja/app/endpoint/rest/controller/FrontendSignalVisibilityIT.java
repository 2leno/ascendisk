package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.model.Direction;
import api.poja.app.model.SignalAssessment;
import api.poja.app.service.SignalService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class FrontendSignalVisibilityIT extends ApiIT {

  @MockBean SignalService signalService;

  @BeforeEach
  void signals() {
    when(signalService.currentSignals())
        .thenReturn(
            List.of(
                signal(73, true),
                signal(75, true),
                signal(79, true),
                signal(80, true),
                signal(82, false),
                signal(85, true)));
  }

  @Test
  void freeUsers_seeOnlyScoresFrom75_andPremiumScoresAreLocked() {
    var response = get("/api/signals", null, FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertArrayEquals(new int[] {75, 79, 80, 82, 85}, scores(response.getBody()));
    assertArrayEquals(new boolean[] {false, false, true, true, true}, locked(response.getBody()));
    assertArrayEquals(new boolean[] {true, true, false, false, false}, advice(response.getBody()));
  }

  @Test
  void admins_seeEverySignal_unlockedWhenTradable() {
    var response = get("/api/signals", adminFirebaseToken(), FrontendSignalResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertArrayEquals(new int[] {73, 75, 79, 80, 82, 85}, scores(response.getBody()));
    assertArrayEquals(
        new boolean[] {false, false, false, false, true, false}, locked(response.getBody()));
  }

  private SignalAssessment signal(int score, boolean tradable) {
    return SignalAssessment.builder()
        .symbol("S" + score)
        .displayName("PAIR-" + score)
        .currentPrice(1.2)
        .score(score)
        .direction(tradable ? Direction.BUY : Direction.NEUTRAL)
        .tradable(tradable)
        .payout("80%")
        .build();
  }

  private int[] scores(FrontendSignalResponse[] signals) {
    return Arrays.stream(signals).mapToInt(FrontendSignalResponse::score).toArray();
  }

  private boolean[] locked(FrontendSignalResponse[] signals) {
    boolean[] values = new boolean[signals.length];
    for (var i = 0; i < signals.length; i++) {
      values[i] = signals[i].locked();
    }
    return values;
  }

  private boolean[] advice(FrontendSignalResponse[] signals) {
    boolean[] values = new boolean[signals.length];
    for (var i = 0; i < signals.length; i++) {
      values[i] = signals[i].advice();
    }
    return values;
  }
}
