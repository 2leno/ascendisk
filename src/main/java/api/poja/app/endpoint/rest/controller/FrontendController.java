package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.LicenseActivationRequest;
import api.poja.app.endpoint.rest.model.response.FrontendSignalResponse;
import api.poja.app.endpoint.rest.model.response.LicenseActivationResponse;
import api.poja.app.mapper.FrontendSignalMapper;
import api.poja.app.service.LicenseService;
import api.poja.app.service.SignalService;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FrontendController {

  private final SignalService signalService;
  private final FrontendSignalMapper frontendSignalMapper;
  private final LicenseService licenseService;

  @GetMapping("/signals")
  public List<FrontendSignalResponse> signals() {
    var signals = signalService.currentSignals();
    return IntStream.range(0, signals.size())
        .mapToObj(index -> frontendSignalMapper.toResponse(signals.get(index), index + 1L))
        .toList();
  }

  @PostMapping("/licenses/activate")
  public LicenseActivationResponse activate(
      @RequestBody(required = false) LicenseActivationRequest request) {
    return licenseService.activate(request == null ? null : request.key());
  }
}
