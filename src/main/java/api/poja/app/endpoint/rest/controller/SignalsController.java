package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.response.SignalResponse;
import api.poja.app.mapper.SignalMapper;
import api.poja.app.service.SignalService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SignalsController {

  private final SignalService signalService;
  private final SignalMapper signalMapper;

  @GetMapping("/signals")
  public List<SignalResponse> signals() {
    return signalService.currentSignals().stream().map(signalMapper::toResponse).toList();
  }

  @GetMapping("/signals/{symbol}")
  public SignalResponse signal(@PathVariable String symbol) {
    return signalMapper.toResponse(signalService.signalFor(symbol));
  }
}
