package api.poja.app.repository;

import api.poja.app.repository.model.JLicenseKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JLicenseKeyRepository extends JpaRepository<JLicenseKey, UUID> {
  Optional<JLicenseKey> findByCodeHash(String codeHash);

  List<JLicenseKey> findAllByOrderByCreatedAtDesc();

  long countByActiveTrue();
}
