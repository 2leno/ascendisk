package api.poja.app.service;

import api.poja.app.endpoint.rest.model.request.AssetInput;
import api.poja.app.exception.ApiException;
import api.poja.app.mapper.AssetMapper;
import api.poja.app.model.Asset;
import api.poja.app.repository.JAssetRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class AssetService {

  private final JAssetRepository assetRepository;
  private final AssetMapper assetMapper;

  @Transactional(readOnly = true)
  public List<Asset> list(Boolean enabled) {
    var assets =
        Boolean.TRUE.equals(enabled)
            ? assetRepository.findAllByEnabledTrue()
            : assetRepository.findAll();
    return assets.stream().map(assetMapper::toDomain).toList();
  }

  @Transactional
  public Asset create(AssetInput input) {
    if (input == null || input.symbol() == null || input.symbol().isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Symbol is required");
    }
    if (assetRepository.existsBySymbol(input.symbol())) {
      throw new ApiException(HttpStatus.CONFLICT, "An asset with this symbol already exists");
    }
    return assetMapper.toDomain(assetRepository.save(assetMapper.toEntity(input)));
  }

  @Transactional
  public Asset update(String symbol, AssetInput input) {
    var asset = assetRepository.findBySymbol(symbol).orElseThrow(this::notFound);
    assetMapper.update(asset, input);
    return assetMapper.toDomain(assetRepository.save(asset));
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Asset not found");
  }
}
