package org.pdf.takeyourtime.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.pdf.takeyourtime.dto.AbsenceDTO;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions.StandInUserIsUserException;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions.StandInUserNotFound;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions.UserNotFound;
import org.pdf.takeyourtime.exceptions.UserExceptions.DoesNotExistException;
import org.pdf.takeyourtime.models.Absence;
import org.pdf.takeyourtime.models.AbsenceType;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.repos.AbsenceRepo;
import org.pdf.takeyourtime.repos.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AbsenceService {

  private final List<AbsenceType> allowedSupervisorAbsenceTypes = List.of(
      AbsenceType.SICK_LEAVE,
      AbsenceType.UNPAID_LEAVE);

  private final AbsenceRepo absenceRepo;
  private final UserRepo userRepo;
  private final UserService userService;
  private final DepartmentService departmentService;

  public AbsenceService(AbsenceRepo absenceRepo, UserRepo userRepo, UserService userService,
      DepartmentService departmentService) {
    this.absenceRepo = absenceRepo;
    this.userRepo = userRepo;
    this.userService = userService;
    this.departmentService = departmentService;
  }

  public Absence absenceFromDTO(AbsenceDTO absenceDTO)
      throws AbsenceExceptions.UserNotFound, AbsenceExceptions.StandInUserNotFound {
    User absentUser;
    try {
      absentUser = (User) userService.loadUserByUsername(absenceDTO.username());
    } catch (UsernameNotFoundException e) {
      throw new AbsenceExceptions.UserNotFound();
    }

    User standInUser = null;
    String standInUsername = absenceDTO.standInUsername();
    if (standInUsername != null) {
      try {
        standInUser = (User) userService.loadUserByUsername(standInUsername);
      } catch (UsernameNotFoundException e) {
        throw new AbsenceExceptions.StandInUserNotFound();
      }
    }

    LocalDate startDate = Instant.ofEpochMilli(Long.parseLong(absenceDTO.startDate()))
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
    LocalDate endDate = Instant.ofEpochMilli(Long.parseLong(absenceDTO.endDate()))
        .atZone(ZoneId.systemDefault())
        .toLocalDate();

    if (startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }

    if (absenceDTO.id() == null) {
      return new Absence(absenceDTO.absenceType(), absentUser, standInUser, startDate,
          endDate, absenceDTO.isApproved());
    }

    return new Absence(absenceDTO.id(), absenceDTO.absenceType(), absentUser, standInUser, startDate,
        endDate, absenceDTO.isApproved());
  }

  private Optional<Absence> checkAndApproveAbsenceIfEligible(Absence absence)
      throws UserNotFound, StandInUserNotFound, DoesNotExistException, AbsenceExceptions.NotEnoughVacationDays,
      AbsenceExceptions.NotEnoughSpecialLeaveDays, AbsenceExceptions.StandInUserIsAbsentException,
      AbsenceExceptions.AlreadyAbsentException, AbsenceExceptions.StandInUserIsUserException {
    if (absence.isApproved()) {
      return Optional.of(absence);
    }

    final User absentUser = absence.getUser();
    if (absentUser == null) {
      throw new AbsenceExceptions.UserNotFound();
    }

    final LocalDate startDate = absence.getStartDate();
    final LocalDate endDate = absence.getEndDate();
    if (userService.isAbsentWhileDate(absentUser, startDate, endDate)) {
      throw new AbsenceExceptions.AlreadyAbsentException();
    }

    final int absenceDuration = userService.getDuration(absence);
    final AbsenceType absenceType = absence.getAbsenceType();
    if (absenceType == AbsenceType.SPECIAL_LEAVE) {
      return handleSpecialLeaveAbsence(absence, absentUser, absenceDuration);
    }

    if (absenceType == AbsenceType.SICK_LEAVE) {
      return Optional.of(approveAndSaveAbsence(absence));
    }

    final User standInUser = absence.getStandInUser();
    if (standInUser == null) {
      throw new AbsenceExceptions.StandInUserNotFound();
    }

    if (absentUser.getId() == standInUser.getId()) {
      throw new AbsenceExceptions.StandInUserIsUserException();
    }

    if (userService.isAbsentWhileDate(standInUser, startDate, endDate)) {
      throw new AbsenceExceptions.StandInUserIsAbsentException();
    }

    switch (absenceType) {
      case VACATION:
        return handleVacationAbsence(absence, absentUser, absenceDuration);
      case UNPAID_LEAVE:
        return Optional.of(absence);
      default:
        return Optional.empty();
    }
  }

  private Optional<Absence> handleVacationAbsence(Absence absence, User absentUser, int absenceDuration)
      throws DoesNotExistException, AbsenceExceptions.NotEnoughVacationDays {
    if (userService.getCommulatedVacationDays(absentUser) < absenceDuration) {
      throw new AbsenceExceptions.NotEnoughVacationDays();
    }

    if (departmentService
        .getAbsentMembers(absence.getUser().getDepartment(), absence.getStartDate(), absence.getEndDate())
        .size() >= 4) {
      return Optional.of(absence);
    }

    return Optional.of(approveAndSaveAbsence(absence));
  }

  private Optional<Absence> handleSpecialLeaveAbsence(Absence absence, User absentUser, int absenceDuration)
      throws DoesNotExistException, AbsenceExceptions.NotEnoughSpecialLeaveDays {
    if (userService.getCommulatedSpecialLeaveDays(absentUser) < absenceDuration) {
      throw new AbsenceExceptions.NotEnoughSpecialLeaveDays();
    }

    return Optional.of(approveAndSaveAbsence(absence));
  }

  public Optional<Absence> addOrEditAbsence(Absence newAbsence, Long requestUserId)
      throws AbsenceExceptions.DoesNotExistException, DoesNotExistException, AbsenceExceptions.NotAllowedToAddOrEdit,
      AbsenceExceptions.NotEnoughVacationDays,
      AbsenceExceptions.NotEnoughSpecialLeaveDays, AbsenceExceptions.StandInUserIsAbsentException,
      AbsenceExceptions.AlreadyAbsentException, UserNotFound, StandInUserNotFound, StandInUserIsUserException,
      AbsenceExceptions.AbsenceAlreadyApprovedException {
    Optional<Absence> dbAbsence;

    Absence checkAbsence = newAbsence;
    if (newAbsence.getId() != null) {
      Optional<Absence> optionalAbsence = getAbsenceById(newAbsence.getId());

      if (optionalAbsence.isPresent()) {
        checkAbsence = optionalAbsence.get();

        if (checkAbsence.isApproved()) {
          throw new AbsenceExceptions.AbsenceAlreadyApprovedException();
        }
      } else {
        throw new AbsenceExceptions.DoesNotExistException();
      }
    }

    if (!isUserAllowedToEditAbsence(checkAbsence, requestUserId)) {
      throw new AbsenceExceptions.NotAllowedToAddOrEdit();
    }

    dbAbsence = checkAndApproveAbsenceIfEligible(newAbsence);

    if (dbAbsence.isPresent() && !dbAbsence.get().isApproved()) {
      return Optional.of(absenceRepo.save(dbAbsence.get()));
    }

    return dbAbsence;
  }

  private boolean isUserAllowedToEditAbsence(Absence absence, Long requestUserId) {
    return absence.getUser().getId() == requestUserId ||
        (isUserSupervisorOfAbsence(absence, requestUserId) &&
            allowedSupervisorAbsenceTypes.contains(absence.getAbsenceType()));
  }

  public List<Absence> getAllAbsences() {
    return absenceRepo.findAll();
  }

  public Optional<Absence> getAbsenceById(Long id) {
    return absenceRepo.findById(id);
  }

  private boolean isUserSupervisorOfAbsence(Absence absence, Long requestUserId) {
    return absence.getUser().getDepartment().getSupervisor().getId() == requestUserId;
  }

  private Absence approveAndSaveAbsence(Absence absence) {
    absence.setApproved(true);
    absenceRepo.save(absence);
    return absence;
  }

  public Optional<Absence> approveAbsenceIfSupervisor(Absence absence, Long approverUserId) {
    if (isUserSupervisorOfAbsence(absence, approverUserId)) {
      return Optional.of(approveAndSaveAbsence(absence));
    }

    return Optional.empty();
  }

  public boolean removeAbsenceById(Long id)
      throws IllegalArgumentException, AbsenceExceptions.NotFoundException,
      AbsenceExceptions.AbsenceAlreadyApprovedException {
    if (id == null) {
      throw new IllegalArgumentException();
    }

    Absence absence = absenceRepo.findById(id).orElseThrow(AbsenceExceptions.NotFoundException::new);

    if (absence.isApproved()) {
      throw new AbsenceExceptions.AbsenceAlreadyApprovedException();
    }

    User user = absence.getUser();
    if (user != null) {
      user.removeAbsence(absence);
      userRepo.save(user);
    }

    user = absence.getStandInUser();
    if (user != null) {
      user.removeStandInAbsence(absence);
      userRepo.save(user);
    }

    absenceRepo.deleteById(id);
    return absenceRepo.findById(id).isEmpty();
  }
}
