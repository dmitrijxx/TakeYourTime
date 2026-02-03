package org.pdf.takeyourtime.dto;

import org.pdf.takeyourtime.models.AbsenceType;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AbsenceDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("absenceType") AbsenceType absenceType,
        @JsonProperty("username") String username,
        @JsonProperty("standInUsername") String standInUsername,
        @JsonProperty("startDate") String startDate,
        @JsonProperty("endDate") String endDate,
        @JsonProperty("isApproved") boolean isApproved) {
}
