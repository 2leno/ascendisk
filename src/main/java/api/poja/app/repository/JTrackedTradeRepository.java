package api.poja.app.repository;

import api.poja.app.model.TradeStatus;
import api.poja.app.repository.model.JTrackedTrade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JTrackedTradeRepository extends JpaRepository<JTrackedTrade, UUID> {
  List<JTrackedTrade> findAllByOrderByOpenedAtDesc();

  List<JTrackedTrade> findAllByStatusOrderByOpenedAtDesc(TradeStatus status);
}
