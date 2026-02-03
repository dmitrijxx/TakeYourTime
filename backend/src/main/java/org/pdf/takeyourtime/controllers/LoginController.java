package org.pdf.takeyourtime.controllers;

import org.pdf.takeyourtime.constants.ErrorConstants;
import org.pdf.takeyourtime.constants.LoginConstants;
import org.pdf.takeyourtime.dto.LoginDTO;
import org.pdf.takeyourtime.dto.LoginResponseDTO;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/login")
public class LoginController {
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;

  public LoginController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
    this.authenticationManager = authenticationManager;
    this.jwtUtils = jwtUtils;
  }

  @PostMapping
  public ResponseEntity<?> login(@RequestBody LoginDTO login) {
    try {
      Authentication auth = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(login.username(), login.password()));
      SecurityContextHolder.getContext().setAuthentication(auth);

      return ResponseEntity.ok().body(new LoginResponseDTO(jwtUtils.createToken(auth), ((User) auth.getPrincipal()).toDTO()));
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginConstants.USER_NOT_FOUND);
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginConstants.WRONG_CREDENTIALS);
    } catch (Exception e) {
      logger.error("An authentication error occurred for user: {}", login.username(), e);
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorConstants.INTERNAL_SERVER_ERROR);
  }
}
