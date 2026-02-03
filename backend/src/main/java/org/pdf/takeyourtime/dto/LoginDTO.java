package org.pdf.takeyourtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginDTO(
  @JsonProperty("username") String username,
  @JsonProperty("password") String password) {
}
