package api.poja.app.repository;

import api.poja.app.repository.model.JAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JAssetRepository extends JpaRepository<JAsset, UUID> {
  Optional<JAsset> findBySymbol(String symbol);

  boolean existsBySymbol(String symbol);

  List<JAsset> findAllByEnabledTrue();
}
