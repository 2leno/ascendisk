package api.poja.app.repository;

import api.poja.app.repository.model.JAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JAccountRepository extends JpaRepository<JAccount, UUID> {
  Optional<JAccount> findByUsername(String username);
}
