package api.poja.app.endpoint.rest.model.response;

public record AdminOverviewResponse(
    long members,
    long activeMembers,
    long licenseKeys,
    long activeLicenseKeys,
    long activeActivations,
    long assets,
    long enabledAssets,
    long signals) {}
