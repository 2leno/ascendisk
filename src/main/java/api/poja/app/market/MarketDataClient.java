package api.poja.app.market;

import api.poja.app.model.Candle;
import java.util.List;

public interface MarketDataClient {

  List<Candle> minuteCandles(String symbol, int limit);

  default List<Candle> candles(String symbol, String interval, int limit) {
    return minuteCandles(symbol, limit);
  }
}
