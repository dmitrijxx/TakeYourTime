package org.pdf.takeyourtime.dto;

import org.pdf.takeyourtime.models.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDTO(
    @JsonProperty("token") String token,
    @JsonProperty("user") UserDTO user) {
}
