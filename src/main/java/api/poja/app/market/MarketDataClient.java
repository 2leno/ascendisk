package api.poja.app.market;

import api.poja.app.model.Candle;
import java.util.List;

public interface MarketDataClient {

  List<Candle> minuteCandles(String symbol, int limit);
}
