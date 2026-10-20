package api.poja.app.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.exception.ApiException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FirebaseTokenVerifierTest {

  private static final Instant NOW = Instant.parse("2024-01-03T12:00:00Z");

  private RSAKey rsaKey;
  private FirebaseTokenVerifier verifier;

  @BeforeEach
  void setUp() throws Exception {
    rsaKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
    var jwkSet = new JWKSet(rsaKey.toPublicJWK());
    JWKSource<SecurityContext> source = (selector, context) -> selector.select(jwkSet);
    verifier = new FirebaseTokenVerifier("ascendisk", source, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void verify_validToken_returnsUser() throws Exception {
    var token =
        sign(
            claims()
                .claim("email", "User@Example.COM")
                .claim("name", "Test User")
                .claim("picture", "https://example.com/avatar.png")
                .claim("email_verified", true));

    var user = verifier.verify(token);

    assertEquals("uid-1", user.uid());
    assertEquals("user@example.com", user.email());
    assertEquals("Test User", user.displayName());
    assertEquals("https://example.com/avatar.png", user.pictureUrl());
    assertTrue(user.emailVerified());
  }

  @Test
  void verify_missingOptionalClaims_returnsEmptyValues() throws Exception {
    var user = verifier.verify(sign(claims()));

    assertEquals(null, user.email());
    assertEquals(null, user.displayName());
    assertFalse(user.emailVerified());
  }

  @Test
  void verify_wrongIssuer_isRejected() throws Exception {
    assertUnauthorized(sign(claims().issuer("https://securetoken.google.com/other")));
  }

  @Test
  void verify_wrongAudience_isRejected() throws Exception {
    assertUnauthorized(sign(claims().audience("other")));
  }

  @Test
  void verify_missingAudience_isRejected() throws Exception {
    assertUnauthorized(sign(claims().audience((String) null)));
  }

  @Test
  void verify_expiredToken_isRejected() throws Exception {
    assertUnauthorized(sign(claims().expirationTime(Date.from(NOW.minusSeconds(1)))));
  }

  @Test
  void verify_missingExpiration_isRejected() throws Exception {
    assertUnauthorized(sign(claims().expirationTime(null)));
  }

  @Test
  void verify_futureIssueTime_isRejected() throws Exception {
    assertUnauthorized(sign(claims().issueTime(Date.from(NOW.plusSeconds(300)))));
  }

  @Test
  void verify_missingSubject_isRejected() throws Exception {
    assertUnauthorized(sign(claims().subject(null)));
  }

  @Test
  void verify_unknownKeyId_isRejected() throws Exception {
    var jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("unknown").build(), claims().build());
    jwt.sign(new RSASSASigner(rsaKey));
    assertUnauthorized(jwt.serialize());
  }

  @Test
  void verify_badSignature_isRejected() throws Exception {
    var otherKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
    var jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            claims().build());
    jwt.sign(new RSASSASigner(otherKey));
    assertUnauthorized(jwt.serialize());
  }

  @Test
  void verify_malformedToken_isRejected() {
    assertUnauthorized("not-a-jwt");
  }

  private JWTClaimsSet.Builder claims() {
    return new JWTClaimsSet.Builder()
        .issuer("https://securetoken.google.com/ascendisk")
        .audience("ascendisk")
        .subject("uid-1")
        .issueTime(Date.from(NOW))
        .expirationTime(Date.from(NOW.plusSeconds(3600)));
  }

  private String sign(JWTClaimsSet.Builder claims) throws Exception {
    var jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            claims.build());
    jwt.sign(new RSASSASigner(rsaKey));
    return jwt.serialize();
  }

  private void assertUnauthorized(String token) {
    var exception = assertThrows(ApiException.class, () -> verifier.verify(token));
    assertEquals(401, exception.getStatus().value());
  }
}
