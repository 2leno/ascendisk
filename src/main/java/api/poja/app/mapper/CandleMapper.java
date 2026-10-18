package api.poja.app.mapper;

import api.poja.app.endpoint.rest.model.response.CandleResponse;
import api.poja.app.model.Candle;
import org.springframework.stereotype.Component;

@Component
public class CandleMapper {

  public CandleResponse toResponse(Candle candle) {
    return CandleResponse.builder()
        .openTime(candle.openTime())
        .open(candle.open())
        .high(candle.high())
        .low(candle.low())
        .close(candle.close())
        .volume(candle.volume())
        .build();
  }
}
