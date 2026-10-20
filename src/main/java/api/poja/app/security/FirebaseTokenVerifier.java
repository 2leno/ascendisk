package api.poja.app.security;

import api.poja.app.exception.ApiException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenVerifier {

  private static final String JWKS_URL =
      "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

  private final String projectId;
  private final JWKSource<SecurityContext> jwkSource;
  private final Clock clock;

  @Autowired
  public FirebaseTokenVerifier(
      @Value("${app.firebase.project-id:ascendisk}") String projectId, Clock clock) {
    this(projectId, remoteJwkSource(), clock);
  }

  FirebaseTokenVerifier(String projectId, JWKSource<SecurityContext> jwkSource, Clock clock) {
    this.projectId = projectId;
    this.jwkSource = jwkSource;
    this.clock = clock;
  }

  public FirebaseUser verify(String token) {
    try {
      var signedJwt = SignedJWT.parse(token);
      verifySignature(signedJwt);

      var claims = signedJwt.getJWTClaimsSet();
      var expectedIssuer = "https://securetoken.google.com/" + projectId;
      if (!expectedIssuer.equals(claims.getIssuer())) {
        throw unauthorized();
      }
      List<String> audience = claims.getAudience();
      if (audience == null || !audience.contains(projectId)) {
        throw unauthorized();
      }
      var expiresAt = claims.getExpirationTime();
      if (expiresAt == null || expiresAt.toInstant().isBefore(clock.instant())) {
        throw unauthorized();
      }
      var issuedAt = claims.getIssueTime();
      if (issuedAt != null && issuedAt.toInstant().isAfter(clock.instant().plusSeconds(120))) {
        throw unauthorized();
      }
      var subject = claims.getSubject();
      if (subject == null || subject.isBlank()) {
        throw unauthorized();
      }

      return new FirebaseUser(
          subject,
          lowerCase(claims.getStringClaim("email")),
          claims.getStringClaim("name"),
          claims.getStringClaim("picture"),
          Boolean.TRUE.equals(claims.getBooleanClaim("email_verified")));
    } catch (ApiException exception) {
      throw exception;
    } catch (Exception exception) {
      throw unauthorized();
    }
  }

  private void verifySignature(SignedJWT signedJwt) throws Exception {
    var keyId = signedJwt.getHeader().getKeyID();
    var selector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
    var keys = jwkSource.get(selector, null);
    if (keys == null || keys.isEmpty()) {
      throw unauthorized();
    }
    JWK jwk = keys.getFirst();
    if (!(jwk instanceof RSAKey rsaKey)) {
      throw unauthorized();
    }
    JWSVerifier verifier = new RSASSAVerifier(rsaKey);
    if (!signedJwt.verify(verifier)) {
      throw unauthorized();
    }
  }

  private static JWKSource<SecurityContext> remoteJwkSource() {
    try {
      return new RemoteJWKSet<>(URI.create(JWKS_URL).toURL());
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to configure Firebase JWKS", exception);
    }
  }

  private static String lowerCase(String value) {
    return value == null ? null : value.toLowerCase();
  }

  private static ApiException unauthorized() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "Token Firebase invalide");
  }
}
