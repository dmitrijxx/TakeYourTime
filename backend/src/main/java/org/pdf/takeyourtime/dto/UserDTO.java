package org.pdf.takeyourtime.dto;

import org.pdf.takeyourtime.models.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(
    @JsonProperty("id") Long id,
    @JsonProperty("disabled") boolean isDisabled,
    @JsonProperty("role") UserRole userRole, // Could lead to errors if the role is not a valid UserRole (UserRole type
                                             // is not checked)
    @JsonProperty("username") String username,
    @JsonProperty("password") String password,
    @JsonProperty("departmentId") Long departmentId,
    @JsonProperty("supervisingDepartmentId") Long supervisingDepartmentId,
    @JsonProperty("vacationDays") Integer vacationDays,
    @JsonProperty("specialLeaveDays") Integer specialLeaveDays){
}
