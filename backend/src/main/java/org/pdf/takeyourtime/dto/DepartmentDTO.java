package org.pdf.takeyourtime.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DepartmentDTO(
  @JsonProperty("id") Long id,
  @JsonProperty("name") String name,
  @JsonProperty("members") Set<UserDTO> members,
  @JsonProperty("supervisorName") String supervisorName){
}
