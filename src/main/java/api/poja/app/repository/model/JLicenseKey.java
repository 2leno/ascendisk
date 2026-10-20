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
@Table(name = "license_key")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JLicenseKey {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "code_hash", nullable = false, unique = true)
  private String codeHash;

  @Column(name = "code_prefix", nullable = false)
  private String codePrefix;

  private String label;

  @Column(name = "max_activations", nullable = false)
  private int maxActivations;

  @Column(name = "activation_count", nullable = false)
  private int activationCount;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "created_by")
  private UUID createdBy;
}
