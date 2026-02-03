package org.pdf.takeyourtime.configuration;

import java.util.Arrays;

import org.pdf.takeyourtime.security.JwtAuthFilter;
import org.pdf.takeyourtime.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${spring.security.url-patterns.api}")
  private String apiPattern;

  @Value("${spring.security.url-patterns.auth}")
  private String authPattern;

  @Value("${spring.security.cors.allowed-origins}")
  private String[] allowedOrigins;

  @Value("${spring.security.cors.allowed-methods}")
  private String[] allowedMethods;

  @Value("${spring.security.cors.allowed-headers}")
  private String[] allowedHeaders;

  private final PasswordEncoder passwordEncoder;
  private final JwtAuthFilter jwtAuthFilter;
  private final UserService userService;

  public SecurityConfig(PasswordEncoder passwordEncoder, JwtAuthFilter jwtAuthFilter, UserService userService) {
    this.passwordEncoder = passwordEncoder;
    this.jwtAuthFilter = jwtAuthFilter;
    this.userService = userService;
  }

  @Bean
  public HttpSessionCsrfTokenRepository csrfTokenRepository() {
    return new HttpSessionCsrfTokenRepository();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authenticationProvider(authenticationProvider())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(apiPattern)
            .csrfTokenRepository(csrfTokenRepository()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(authPattern).permitAll()
            .anyRequest().authenticated())
        .sessionManagement(ses -> ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowCredentials(false);
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
    configuration.setAllowedMethods(Arrays.asList(allowedMethods));
    configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(apiPattern, configuration);

    return source;
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

    provider.setPasswordEncoder(passwordEncoder);
    provider.setUserDetailsService(userService);

    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
