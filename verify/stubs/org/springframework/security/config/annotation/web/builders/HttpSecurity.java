package org.springframework.security.config.annotation.web.builders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

public class HttpSecurity {
  public interface CsrfConfigurer { CsrfConfigurer disable(); }
  public interface SessionConfigurer { SessionConfigurer sessionCreationPolicy(SessionCreationPolicy policy); }
  public interface AuthorizedUrl { AuthorizationRegistry permitAll(); AuthorizationRegistry hasAuthority(String a); AuthorizationRegistry authenticated(); }
  public interface AuthorizationRegistry {
    AuthorizedUrl requestMatchers(String... patterns);
    AuthorizedUrl requestMatchers(HttpMethod method, String... patterns);
    AuthorizedUrl anyRequest();
  }
  public interface JwtConfigurer {}
  public interface OAuth2ResourceServerConfigurer { OAuth2ResourceServerConfigurer jwt(Customizer<JwtConfigurer> c); }

  public HttpSecurity csrf(Customizer<CsrfConfigurer> c) { return this; }
  public HttpSecurity sessionManagement(Customizer<SessionConfigurer> c) { return this; }
  public HttpSecurity authorizeHttpRequests(Customizer<AuthorizationRegistry> c) { return this; }
  public HttpSecurity oauth2ResourceServer(Customizer<OAuth2ResourceServerConfigurer> c) { return this; }
  public SecurityFilterChain build() throws Exception { return null; }
}
