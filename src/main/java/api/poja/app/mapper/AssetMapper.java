package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.request.AssetInput;
import api.poja.app.endpoint.rest.model.response.AssetResponse;
import api.poja.app.model.Asset;
import api.poja.app.repository.model.JAsset;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

  public Asset toDomain(JAsset asset) {
    return Asset.builder()
        .id(asset.getId())
        .symbol(asset.getSymbol())
        .displayName(asset.getDisplayName())
        .category(asset.getCategory())
        .payout(asset.getPayout())
        .enabled(asset.isEnabled())
        .build();
  }

  public Asset toDomain(AssetInput input) {
    return Asset.builder()
        .symbol(input.symbol())
        .displayName(input.displayName())
        .category(input.category())
        .payout(input.payout())
        .enabled(input.enabled() == null || input.enabled())
        .build();
  }

  public AssetResponse toResponse(Asset asset) {
    return AssetResponse.builder()
        .symbol(asset.symbol())
        .displayName(asset.displayName())
        .category(asset.category())
        .payout(asset.payout())
        .enabled(asset.enabled())
        .build();
  }
}
