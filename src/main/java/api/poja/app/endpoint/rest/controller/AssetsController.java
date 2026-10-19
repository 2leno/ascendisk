package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.request.AssetInput;
import api.poja.app.endpoint.rest.model.response.AssetResponse;
import api.poja.app.mapper.AssetMapper;
import api.poja.app.service.AssetService;
import java.util.List;
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
public class AssetsController {

  private final AssetService assetService;
  private final AssetMapper assetMapper;

  @GetMapping("/assets")
  public List<AssetResponse> list(@RequestParam(required = false) Boolean enabled) {
    return assetService.list(enabled).stream().map(assetMapper::toResponse).toList();
  }

  @PostMapping("/assets")
  @ResponseStatus(HttpStatus.CREATED)
  public AssetResponse create(@RequestBody AssetInput input) {
    return assetMapper.toResponse(assetService.create(input));
  }

  @PutMapping("/assets/{symbol}")
  public AssetResponse update(@PathVariable String symbol, @RequestBody AssetInput input) {
    return assetMapper.toResponse(assetService.update(symbol, input));
  }
}
