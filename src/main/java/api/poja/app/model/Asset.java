package api.poja.app.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Asset(
    UUID id,
    String symbol,
    String displayName,
    AssetCategory category,
    String payout,
    boolean enabled) {}
