package api.poja.app.service;

import api.poja.app.exception.ApiException;
import api.poja.app.mapper.AssetMapper;
import api.poja.app.market.MarketCalendar;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.Asset;
import api.poja.app.model.Candle;
import api.poja.app.model.SignalAssessment;
import api.poja.app.repository.JAssetRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SignalService {

  private static final String DEFAULT_CANDLE_INTERVAL = "1m";
  private static final Set<String> CANDLE_INTERVALS = Set.of("1m", "2m", "5m", "15m", "30m", "1h");

  private final JAssetRepository assetRepository;
  private final AssetMapper assetMapper;
  private final MarketDataClient marketDataClient;
  private final SignalScoringService signalScoringService;
  private final MarketCalendar marketCalendar;
  private final Clock clock;
  private final int candleLimit;

  public SignalService(
      JAssetRepository assetRepository,
      AssetMapper assetMapper,
      MarketDataClient marketDataClient,
      SignalScoringService signalScoringService,
      MarketCalendar marketCalendar,
      Clock clock,
      @Value("${app.market.yahoo.candle-limit:120}") int candleLimit) {
    this.assetRepository = assetRepository;
    this.assetMapper = assetMapper;
    this.marketDataClient = marketDataClient;
    this.signalScoringService = signalScoringService;
    this.marketCalendar = marketCalendar;
    this.clock = clock;
    this.candleLimit = candleLimit;
  }

  public List<SignalAssessment> currentSignals() {
    var otcClosed = marketCalendar.isOtcClosed();
    var evaluatedAt = clock.instant();
    return assetRepository.findAllByEnabledTrue().stream()
        .map(assetMapper::toDomain)
        .map(asset -> assess(asset, otcClosed, evaluatedAt))
        .sorted(Comparator.comparingDouble(signal -> Math.abs(signal.distance())))
        .toList();
  }

  public SignalAssessment signalFor(String symbol) {
    var asset =
        assetRepository.findBySymbol(symbol).map(assetMapper::toDomain).orElseThrow(this::notFound);
    return assess(asset, marketCalendar.isOtcClosed(), clock.instant());
  }

  public List<Candle> candles(String symbol, int limit) {
    return candles(symbol, DEFAULT_CANDLE_INTERVAL, limit);
  }

  public List<Candle> candles(String symbol, String interval, int limit) {
    if (!assetRepository.existsBySymbol(symbol)) {
      throw notFound();
    }
    if (!CANDLE_INTERVALS.contains(interval)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported candle interval");
    }
    return marketDataClient.candles(symbol, interval, limit);
  }

  private SignalAssessment assess(Asset asset, boolean otcClosed, Instant evaluatedAt) {
    return signalScoringService.assess(
        asset, marketDataClient.minuteCandles(asset.symbol(), candleLimit), otcClosed, evaluatedAt);
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Asset not found");
  }
}
