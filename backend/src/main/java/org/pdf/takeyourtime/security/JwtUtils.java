package org.pdf.takeyourtime.security;

import java.util.Date;

import org.pdf.takeyourtime.configuration.AppConfig;
import org.pdf.takeyourtime.constants.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${default.application.short:TYT}")
  private String shortApplicationName;

  private final AppConfig config;

  public JwtUtils(AppConfig config) {
    this.config = config;
  }

  public String createToken(Authentication auth) {
    final Date currentDate = new Date();
    final Date expirationDate = new Date(currentDate.getTime() + config.getJwtExpirationMs());

    try {
      return JWT.create()
          .withIssuer(shortApplicationName)
          .withSubject(auth.getName())
          .withIssuedAt(currentDate)
          .withExpiresAt(expirationDate)
          .sign(Algorithm.HMAC256(config.getSecret()));
    } catch (JWTCreationException e) {
      throw new RuntimeException("You need to enable Algorithm.HMAC256");
    }
  }

  public String getToken(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }

    if (token.startsWith("Bearer ")) {
      return token.substring(7);
    }

    return token;
  }

  public boolean isValid(String token) {
    try {
      if (token == null || token.isBlank()) {
        return false;
      }

      return getDecodedToken(token) != null;
    } catch (TokenExpiredException e) {
      return false;
    }
  }

  public String getUsername(String token) {
    final DecodedJWT decoded = getDecodedToken(token);

    if (decoded == null) {
      return null;
    }

    return decoded.getSubject();
  }

  private DecodedJWT getDecodedToken(String token) {
    try {
      token = getToken(token);
      JWTVerifier verifier = JWT.require(Algorithm.HMAC256(config.getSecret()))
          .withIssuer(shortApplicationName)
          .build();

      return verifier.verify(token);
    } catch (JWTDecodeException e) {
      logger.error(ErrorConstants.DECODING_JWT, e);
    }

    return null;
  }
}
