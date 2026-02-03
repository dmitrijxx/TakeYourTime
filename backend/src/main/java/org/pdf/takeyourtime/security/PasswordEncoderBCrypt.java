package org.pdf.takeyourtime.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderBCrypt {

  @Value("${spring.security.encoding-strength:10}")
  private int encodingStrength;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(encodingStrength);
  }
}