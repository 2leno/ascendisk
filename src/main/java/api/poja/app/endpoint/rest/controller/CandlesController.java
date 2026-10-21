package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.response.CandleResponse;
import api.poja.app.mapper.CandleMapper;
import api.poja.app.service.SignalService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CandlesController {

  private final SignalService signalService;
  private final CandleMapper candleMapper;

  @GetMapping("/assets/{symbol}/candles")
  public List<CandleResponse> candles(
      @PathVariable String symbol,
      @RequestParam(defaultValue = "1m") String interval,
      @RequestParam(defaultValue = "60") int limit) {
    return signalService.candles(symbol, interval, limit).stream()
        .map(candleMapper::toResponse)
        .toList();
  }
}
