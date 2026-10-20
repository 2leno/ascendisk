package api.poja.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final FirebaseTokenVerifier tokenVerifier;
  private final Set<String> adminEmails;

  public FirebaseAuthFilter(
      FirebaseTokenVerifier tokenVerifier,
      @Value("${app.admin.emails:hei.lucas.13@gmail.com}") String adminEmails) {
    this.tokenVerifier = tokenVerifier;
    this.adminEmails =
        Arrays.stream(adminEmails.split(","))
            .map(String::trim)
            .filter(email -> !email.isEmpty())
            .map(email -> email.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);
    if (token != null) {
      try {
        var user = tokenVerifier.verify(token);
        var role = user.email() != null && adminEmails.contains(user.email()) ? "ADMIN" : "USER";
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (RuntimeException ignored) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    var header = request.getHeader("Authorization");
    return header != null && header.startsWith(BEARER_PREFIX)
        ? header.substring(BEARER_PREFIX.length())
        : null;
  }
}
