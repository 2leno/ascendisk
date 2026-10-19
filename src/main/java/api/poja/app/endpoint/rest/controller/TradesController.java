package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.TradeInput;
import api.poja.app.endpoint.rest.model.request.TradeUpdate;
import api.poja.app.endpoint.rest.model.response.TradeResponse;
import api.poja.app.mapper.TradeMapper;
import api.poja.app.model.TradeStatus;
import api.poja.app.service.TradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TradesController {

  private final TradeService tradeService;
  private final TradeMapper tradeMapper;

  @GetMapping("/trades")
  public List<TradeResponse> list(@RequestParam(required = false) TradeStatus status) {
    return tradeService.list(status).stream().map(tradeMapper::toResponse).toList();
  }

  @PostMapping("/trades")
  @ResponseStatus(HttpStatus.CREATED)
  public TradeResponse track(@RequestBody TradeInput input) {
    return tradeMapper.toResponse(tradeService.track(input));
  }

  @GetMapping("/trades/{tradeId}")
  public TradeResponse get(@PathVariable UUID tradeId) {
    return tradeMapper.toResponse(tradeService.get(tradeId));
  }

  @PutMapping("/trades/{tradeId}")
  public TradeResponse close(@PathVariable UUID tradeId, @RequestBody TradeUpdate input) {
    return tradeMapper.toResponse(tradeService.close(tradeId, input));
  }
}
