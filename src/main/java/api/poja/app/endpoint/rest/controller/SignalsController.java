package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.response.SignalResponse;
import api.poja.app.mapper.SignalMapper;
import api.poja.app.service.SignalService;
import api.poja.app.service.SignalStreamService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@AllArgsConstructor
public class SignalsController {

  private final SignalService signalService;
  private final SignalStreamService signalStreamService;
  private final SignalMapper signalMapper;

  @GetMapping("/signals")
  public List<SignalResponse> signals() {
    return signalService.currentSignals().stream().map(signalMapper::toResponse).toList();
  }

  @GetMapping(value = "/signals/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    return signalStreamService.stream();
  }

  @GetMapping("/signals/{symbol}")
  public SignalResponse signal(@PathVariable String symbol) {
    return signalMapper.toResponse(signalService.signalFor(symbol));
  }
}
