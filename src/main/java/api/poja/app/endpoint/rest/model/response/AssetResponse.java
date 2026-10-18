package api.poja.app.endpoint.rest.model.response;

import api.poja.app.model.AssetCategory;
import lombok.Builder;

@Builder
public record AssetResponse(
    String symbol, String displayName, AssetCategory category, String payout, boolean enabled) {}
