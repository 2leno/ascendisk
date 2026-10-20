package api.poja.app.endpoint.rest.controller;

import static org.mockito.Mockito.when;

import api.poja.app.conf.FacadeIT;
import api.poja.app.security.FirebaseTokenVerifier;
import api.poja.app.security.FirebaseUser;
import api.poja.app.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

public abstract class ApiIT extends FacadeIT {

  @Autowired protected TestRestTemplate restTemplate;

  @Autowired protected JwtTokenProvider jwtTokenProvider;

  @MockBean protected FirebaseTokenVerifier firebaseTokenVerifier;

  protected String firebaseToken(String email) {
    var token = "firebase-" + email;
    var user = new FirebaseUser("uid-" + email, email, "Test " + email, null, true);
    when(firebaseTokenVerifier.verify(token)).thenReturn(user);
    return token;
  }

  protected String adminFirebaseToken() {
    return firebaseToken("hei.lucas.13@gmail.com");
  }

  @BeforeEach
  void disableStreaming() {
    restTemplate.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
  }

  protected String token(String role) {
    return token("admin", role);
  }

  protected String token(String username, String role) {
    return jwtTokenProvider.generateToken(username, List.of(role));
  }

  protected <T> ResponseEntity<T> get(String path, String token, Class<T> responseType) {
    return exchange(path, HttpMethod.GET, token, null, responseType);
  }

  protected <T> ResponseEntity<T> post(
      String path, String token, Object body, Class<T> responseType) {
    return exchange(path, HttpMethod.POST, token, body, responseType);
  }

  protected <T> ResponseEntity<T> put(
      String path, String token, Object body, Class<T> responseType) {
    return exchange(path, HttpMethod.PUT, token, body, responseType);
  }

  private <T> ResponseEntity<T> exchange(
      String path, HttpMethod method, String token, Object body, Class<T> responseType) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), responseType);
  }
}
