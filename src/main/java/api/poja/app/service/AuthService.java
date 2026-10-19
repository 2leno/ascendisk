package api.poja.app.service;

import api.poja.app.endpoint.rest.model.response.LoginResponse;
import api.poja.app.exception.ApiException;
import api.poja.app.repository.JUserRepository;
import api.poja.app.security.JwtTokenProvider;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthService {

  private final JUserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final BCryptPasswordEncoder passwordEncoder;

  public LoginResponse login(String username, String password) {
    if (username == null || password == null) {
      throw invalidCredentials();
    }
    var user = userRepository.findByUsername(username).orElseThrow(this::invalidCredentials);
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw invalidCredentials();
    }
    var token = jwtTokenProvider.generateToken(username, List.of(user.getRole().name()));
    return LoginResponse.builder().token(token).role(user.getRole()).build();
  }

  private ApiException invalidCredentials() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }
}
