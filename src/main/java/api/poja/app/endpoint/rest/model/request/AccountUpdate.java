package api.poja.app.endpoint.rest.model.request;

import lombok.Builder;

@Builder
public record AccountUpdate(Double balance) {}
