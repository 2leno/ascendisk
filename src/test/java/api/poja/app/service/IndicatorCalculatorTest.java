package api.poja.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IndicatorCalculatorTest {

  private final IndicatorCalculator calculator = new IndicatorCalculator();

  @Test
  void ema_ofEmptyValues_returnsZero() {
    assertEquals(0.0, calculator.ema(List.of(), 9), 1e-9);
  }

  @Test
  void ema_ofConstantSeries_returnsThatValue() {
    assertEquals(5.0, calculator.ema(List.of(5.0, 5.0, 5.0, 5.0, 5.0), 9), 1e-9);
  }

  @Test
  void ema_ofRisingSeries_liesBetweenFirstAndLastValue() {
    var values = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);

    var ema = calculator.ema(values, 9);

    assertTrue(ema > values.get(0));
    assertTrue(ema < values.get(values.size() - 1));
  }

  @Test
  void rsi_ofInsufficientData_isNeutral() {
    assertEquals(50.0, calculator.rsi(List.of(1.0, 2.0), 14), 1e-9);
  }

  @Test
  void rsi_ofStrictlyRisingSeries_isMax() {
    var values =
        List.of(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0);

    assertEquals(100.0, calculator.rsi(values, 14), 1e-9);
  }

  @Test
  void rsi_ofStrictlyFallingSeries_isZero() {
    var values =
        List.of(
            16.0, 15.0, 14.0, 13.0, 12.0, 11.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0);

    assertEquals(0.0, calculator.rsi(values, 14), 1e-9);
  }
}
