package org.pdf.takeyourtime.configuration;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Configuration
public class AppConfig {

  @Value("${default.date-time-format:yyyy-MM-dd}")
  private String defaultDateTimeFormat;

  @Value("${spring.security.jwt.secret}")
  private String jwtSecuritySecret;

  @Value("${spring.security.jwt.expiration-ms}")
  private long jwtExpirationMs;

  private final String randomSecret;

  public AppConfig() {
    randomSecret = String.valueOf(new SecureRandom().nextLong());
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(defaultDateTimeFormat);

    return builder -> {
      builder.dateFormat(simpleDateFormat);
      builder.simpleDateFormat(defaultDateTimeFormat);
      builder.serializers(new DateSerializer(false, simpleDateFormat));
      builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(defaultDateTimeFormat)));
      builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(defaultDateTimeFormat)));
    };
  }

  public String getSecret() {
    if (jwtSecuritySecret == null || jwtSecuritySecret.isBlank()) {
      return randomSecret;
    }

    return jwtSecuritySecret;
  }

  public long getJwtExpirationMs() {
    return jwtExpirationMs;
  }
}
