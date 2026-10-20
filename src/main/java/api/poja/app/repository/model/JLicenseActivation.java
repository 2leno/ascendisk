package api.poja.app.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "license_activation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JLicenseActivation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "license_key_id", nullable = false)
  private UUID licenseKeyId;

  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "activated_at", nullable = false)
  private Instant activatedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;
}
