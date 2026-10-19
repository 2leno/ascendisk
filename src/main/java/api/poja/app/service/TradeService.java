package api.poja.app.service;

import api.poja.app.endpoint.rest.model.request.TradeInput;
import api.poja.app.endpoint.rest.model.request.TradeUpdate;
import api.poja.app.exception.ApiException;
import api.poja.app.mapper.TradeMapper;
import api.poja.app.model.Direction;
import api.poja.app.model.TrackedTrade;
import api.poja.app.model.TradeStatus;
import api.poja.app.repository.JAssetRepository;
import api.poja.app.repository.JTrackedTradeRepository;
import api.poja.app.repository.model.JTrackedTrade;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class TradeService {

  private final JTrackedTradeRepository tradeRepository;
  private final JAssetRepository assetRepository;
  private final SignalService signalService;
  private final TradeMapper tradeMapper;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<TrackedTrade> list(TradeStatus status) {
    var trades =
        status == null
            ? tradeRepository.findAllByOrderByOpenedAtDesc()
            : tradeRepository.findAllByStatusOrderByOpenedAtDesc(status);
    return trades.stream().map(tradeMapper::toDomain).toList();
  }

  @Transactional(readOnly = true)
  public TrackedTrade get(UUID id) {
    return tradeMapper.toDomain(tradeRepository.findById(id).orElseThrow(this::notFound));
  }

  @Transactional
  public TrackedTrade track(TradeInput input) {
    if (input == null || input.symbol() == null || input.symbol().isBlank()) {
      throw assetNotFound();
    }
    if (input.direction() == null
        || input.direction() == Direction.NEUTRAL
        || input.zonePrice() == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid direction or missing zone price");
    }
    var asset = assetRepository.findBySymbol(input.symbol()).orElseThrow(this::assetNotFound);
    var signal = signalService.signalFor(input.symbol());
    var trade =
        JTrackedTrade.builder()
            .asset(asset)
            .direction(input.direction())
            .zonePrice(input.zonePrice())
            .entryScore(signal.score())
            .payout(payout(asset.getPayout()))
            .status(TradeStatus.PENDING)
            .openedAt(clock.instant())
            .build();
    return tradeMapper.toDomain(tradeRepository.save(trade));
  }

  @Transactional
  public TrackedTrade close(UUID id, TradeUpdate update) {
    if (update == null || update.status() == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Status is required");
    }
    var trade = tradeRepository.findById(id).orElseThrow(this::notFound);
    trade.setStatus(update.status());
    trade.setResult(update.result());
    if (update.status() != TradeStatus.PENDING) {
      trade.setClosedAt(clock.instant());
    }
    return tradeMapper.toDomain(tradeRepository.save(trade));
  }

  private String payout(String payout) {
    return payout == null || payout.isBlank() ? "-" : payout;
  }

  private ApiException notFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Trade not found");
  }

  private ApiException assetNotFound() {
    return new ApiException(HttpStatus.NOT_FOUND, "Asset not found");
  }
}
