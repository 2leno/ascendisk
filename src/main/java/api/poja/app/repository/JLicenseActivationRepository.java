package api.poja.app.repository;

import api.poja.app.repository.model.JLicenseActivation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JLicenseActivationRepository extends JpaRepository<JLicenseActivation, UUID> {
  Optional<JLicenseActivation> findByLicenseKeyIdAndMemberId(UUID licenseKeyId, UUID memberId);

  Optional<JLicenseActivation> findFirstByMemberIdAndRevokedAtIsNullOrderByActivatedAtDesc(
      UUID memberId);

  long countByLicenseKeyIdAndRevokedAtIsNull(UUID licenseKeyId);

  List<JLicenseActivation> findAllByLicenseKeyIdOrderByActivatedAtDesc(UUID licenseKeyId);

  List<JLicenseActivation> findAllByMemberIdOrderByActivatedAtDesc(UUID memberId);

  long countByRevokedAtIsNull();
}
