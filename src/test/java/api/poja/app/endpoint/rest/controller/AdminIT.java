package api.poja.app.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.endpoint.rest.model.response.AdminOverviewResponse;
import api.poja.app.endpoint.rest.model.response.GeneratedLicenseResponse;
import api.poja.app.endpoint.rest.model.response.LicenseKeyResponse;
import api.poja.app.endpoint.rest.model.response.MemberResponse;
import api.poja.app.market.MarketDataClient;
import api.poja.app.model.MemberStatus;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

class AdminIT extends ApiIT {

  @MockBean MarketDataClient marketDataClient;

  @Test
  void overview_returnsCounts() {
    var response = get("/api/admin/overview", adminFirebaseToken(), AdminOverviewResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().assets() >= 8);
    assertTrue(response.getBody().enabledAssets() >= 8);
  }

  @Test
  void adminEndpoints_rejectNonAdmins() {
    var nonAdmin = get("/api/admin/overview", firebaseToken("not-admin@test.local"), String.class);
    assertEquals(HttpStatus.FORBIDDEN, nonAdmin.getStatusCode());

    var legacyAdmin = get("/api/admin/overview", token("ADMIN"), String.class);
    assertEquals(HttpStatus.FORBIDDEN, legacyAdmin.getStatusCode());
  }

  @Test
  void generateAndRevokeKey() {
    var generated =
        post(
            "/api/admin/keys",
            adminFirebaseToken(),
            Map.of("label", "Test key", "maxActivations", 2),
            GeneratedLicenseResponse.class);

    assertEquals(HttpStatus.CREATED, generated.getStatusCode());
    assertTrue(generated.getBody().code().matches("ASK(-[A-Z2-9]{4}){3}"));
    assertNotNull(generated.getBody().key().id());

    var keys = get("/api/admin/keys", adminFirebaseToken(), LicenseKeyResponse[].class);
    assertTrue(
        Arrays.stream(keys.getBody())
            .anyMatch(key -> key.id().equals(generated.getBody().key().id())));

    var revoked =
        post(
            "/api/admin/keys/" + generated.getBody().key().id() + "/revoke",
            adminFirebaseToken(),
            null,
            Void.class);
    assertEquals(HttpStatus.NO_CONTENT, revoked.getStatusCode());
  }

  @Test
  void blockAndUnblockMember() {
    var email = "member-" + UUID.randomUUID() + "@test.local";
    var token = firebaseToken(email);
    var current = get("/api/me", token, MemberResponse.class);

    assertEquals(HttpStatus.OK, current.getStatusCode());
    assertEquals(MemberStatus.ACTIVE, current.getBody().status());
    assertFalse(current.getBody().license().active());

    var members = get("/api/admin/members", adminFirebaseToken(), MemberResponse[].class);
    var member =
        Arrays.stream(members.getBody())
            .filter(item -> item.email().equals(email))
            .findFirst()
            .orElseThrow();

    var blocked =
        post(
            "/api/admin/members/" + member.id() + "/block",
            adminFirebaseToken(),
            null,
            MemberResponse.class);
    assertEquals(HttpStatus.OK, blocked.getStatusCode());
    assertEquals(MemberStatus.BLOCKED, blocked.getBody().status());
    assertEquals(HttpStatus.FORBIDDEN, get("/api/me", token, String.class).getStatusCode());

    var unblocked =
        post(
            "/api/admin/members/" + member.id() + "/unblock",
            adminFirebaseToken(),
            null,
            MemberResponse.class);
    assertEquals(HttpStatus.OK, unblocked.getStatusCode());
    assertEquals(MemberStatus.ACTIVE, unblocked.getBody().status());
  }

  @Test
  void me_returnsLicenseStatus() {
    var response = get("/api/me", firebaseToken("premium@test.local"), MemberResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("premium@test.local", response.getBody().email());
    assertFalse(response.getBody().license().active());
  }

  @Test
  void adminSignals_includeLockedSignals() {
    var response = get("/api/admin/signals", adminFirebaseToken(), Object[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}
