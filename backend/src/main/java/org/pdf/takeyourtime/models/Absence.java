package org.pdf.takeyourtime.models;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.pdf.takeyourtime.constants.AbsenceConstants;
import org.pdf.takeyourtime.dto.AbsenceDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Absence implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AbsenceType absenceType;

  @ManyToOne
  private User user;

  @ManyToOne
  private User standInUser;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Column(nullable = false)
  private boolean isApproved = false;

  public Absence() {
    this.isApproved = false;
  }

  public Absence(AbsenceType absenceType, User user, User standInUser, LocalDate startDate, LocalDate endDate,
      boolean isApproved) {
    this.absenceType = absenceType;
    this.user = user;
    this.standInUser = standInUser;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isApproved = isApproved;
  }

  public Absence(Long id, AbsenceType absenceType, User absentUser, User standInUser, LocalDate startDate,
      LocalDate endDate, boolean approved) {
    this(absenceType, absentUser, standInUser, startDate, endDate, approved);
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public User getStandInUser() {
    return standInUser;
  }

  public AbsenceType getAbsenceType() {
    return absenceType;
  }

  public void setApproved(boolean approved) {
    isApproved = approved;
  }

  public boolean isApproved() {
    return isApproved;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isWhileDate(LocalDate checkDate) {
    return (startDate.isBefore(checkDate) || startDate.isEqual(checkDate)) &&
        (endDate.isEqual(checkDate) || endDate.isAfter(checkDate));
  }

  public AbsenceDTO toDTO() {
    return new AbsenceDTO(id, absenceType, user.getUsername(), standInUser != null ? standInUser.getUsername() : AbsenceConstants.DELETED_USER, startDate.toString(), endDate.toString(),
        isApproved);
  }

  public void setStandInUser(User user) {
    this.standInUser = user;
  }
}
