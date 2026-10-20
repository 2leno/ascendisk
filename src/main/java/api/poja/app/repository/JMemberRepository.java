package api.poja.app.repository;

import api.poja.app.model.MemberStatus;
import api.poja.app.repository.model.JMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JMemberRepository extends JpaRepository<JMember, UUID> {
  Optional<JMember> findByFirebaseUid(String firebaseUid);

  Optional<JMember> findByEmail(String email);

  List<JMember> findAllByOrderByCreatedAtDesc();

  long countByStatus(MemberStatus status);
}
