package api.poja.app.endpoint.rest.model.request;

import api.poja.app.model.AssetCategory;
import lombok.Builder;

@Builder
public record AssetInput(
    String symbol, String displayName, AssetCategory category, String payout, Boolean enabled) {}
