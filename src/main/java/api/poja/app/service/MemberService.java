package api.poja.app.service;

import api.poja.app.endpoint.rest.model.response.MemberResponse;
import api.poja.app.exception.ApiException;
import api.poja.app.model.MemberRole;
import api.poja.app.model.MemberStatus;
import api.poja.app.repository.JMemberRepository;
import api.poja.app.repository.model.JMember;
import api.poja.app.security.FirebaseUser;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberService {

  private final JMemberRepository memberRepository;
  private final LicenseService licenseService;
  private final Set<String> adminEmails;
  private final Clock clock;

  public MemberService(
      JMemberRepository memberRepository,
      LicenseService licenseService,
      @Value("${app.admin.emails:hei.lucas.13@gmail.com}") String adminEmails,
      Clock clock) {
    this.memberRepository = memberRepository;
    this.licenseService = licenseService;
    this.adminEmails =
        Arrays.stream(adminEmails.split(","))
            .map(String::trim)
            .filter(email -> !email.isEmpty())
            .map(email -> email.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    this.clock = clock;
  }

  @Transactional
  public JMember sync(FirebaseUser firebaseUser) {
    if (firebaseUser == null || firebaseUser.uid() == null || firebaseUser.uid().isBlank()) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Utilisateur Firebase invalide");
    }
    var email =
        firebaseUser.email() == null || firebaseUser.email().isBlank()
            ? firebaseUser.uid() + "@firebase.local"
            : firebaseUser.email().toLowerCase(Locale.ROOT);

    var member =
        memberRepository
            .findByFirebaseUid(firebaseUser.uid())
            .or(() -> memberRepository.findByEmail(email))
            .orElseGet(JMember::new);

    var now = clock.instant();
    if (member.getId() == null) {
      member.setCreatedAt(now);
      member.setStatus(MemberStatus.ACTIVE);
    }
    member.setFirebaseUid(firebaseUser.uid());
    member.setEmail(email);
    member.setDisplayName(displayName(firebaseUser, email));
    member.setRole(adminEmails.contains(email) ? MemberRole.ADMIN : MemberRole.USER);
    member.setLastSeenAt(now);
    return memberRepository.save(member);
  }

  @Transactional
  public JMember requireActive(FirebaseUser firebaseUser) {
    var member = sync(firebaseUser);
    if (member.getStatus() == MemberStatus.BLOCKED) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Compte bloqué");
    }
    return member;
  }

  @Transactional(readOnly = true)
  public void requireNotBlocked(FirebaseUser firebaseUser) {
    if (firebaseUser == null) {
      return;
    }
    memberRepository
        .findByFirebaseUid(firebaseUser.uid())
        .filter(member -> member.getStatus() == MemberStatus.BLOCKED)
        .ifPresent(
            member -> {
              throw new ApiException(HttpStatus.FORBIDDEN, "Compte bloqué");
            });
  }

  @Transactional
  public JMember requireAdmin(FirebaseUser firebaseUser) {
    var member = requireActive(firebaseUser);
    if (member.getRole() != MemberRole.ADMIN) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Accès administrateur requis");
    }
    return member;
  }

  @Transactional(readOnly = true)
  public MemberResponse current(FirebaseUser firebaseUser) {
    var member =
        memberRepository
            .findByFirebaseUid(firebaseUser.uid())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membre introuvable"));
    return toResponse(member);
  }

  @Transactional(readOnly = true)
  public MemberResponse responseFor(JMember member) {
    return toResponse(member);
  }

  @Transactional(readOnly = true)
  public List<MemberResponse> list() {
    return memberRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
  }

  @Transactional
  public MemberResponse setBlocked(UUID memberId, boolean blocked) {
    var member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Membre introuvable"));
    if (member.getRole() == MemberRole.ADMIN && blocked) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Impossible de bloquer un administrateur");
    }
    member.setStatus(blocked ? MemberStatus.BLOCKED : MemberStatus.ACTIVE);
    return toResponse(memberRepository.save(member));
  }

  @Transactional(readOnly = true)
  public boolean hasPremiumAccess(FirebaseUser firebaseUser) {
    if (firebaseUser == null) {
      return false;
    }
    if (isAdminEmail(firebaseUser.email())) {
      return true;
    }
    var member = memberRepository.findByFirebaseUid(firebaseUser.uid()).orElse(null);
    if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
      return false;
    }
    return member.getRole() == MemberRole.ADMIN || licenseService.hasActiveLicense(member);
  }

  public boolean isAdminEmail(String email) {
    return email != null && adminEmails.contains(email.toLowerCase(Locale.ROOT));
  }

  private MemberResponse toResponse(JMember member) {
    return new MemberResponse(
        member.getId(),
        member.getEmail(),
        member.getDisplayName(),
        member.getRole(),
        member.getStatus(),
        member.getCreatedAt(),
        member.getLastSeenAt(),
        licenseService.statusFor(member.getId()));
  }

  private String displayName(FirebaseUser firebaseUser, String fallbackEmail) {
    if (firebaseUser.displayName() != null && !firebaseUser.displayName().isBlank()) {
      return firebaseUser.displayName().trim();
    }
    return fallbackEmail;
  }
}
