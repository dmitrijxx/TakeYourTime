package org.pdf.takeyourtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatsDTO(
    @JsonProperty("sickLeave") Integer sickLeave,
    @JsonProperty("unpaidLeave") Integer unpaidLeave,
    @JsonProperty("vacationDays") Integer vacationDays,
    @JsonProperty("vacationDaysTaken") Integer vacationDaysTaken,
    @JsonProperty("specialLeaveDays") Integer specialLeaveDays,
    @JsonProperty("specialLeaveDaysTaken") Integer specialLeaveDaysTaken){
}
