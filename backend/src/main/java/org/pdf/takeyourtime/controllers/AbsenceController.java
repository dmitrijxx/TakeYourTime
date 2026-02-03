package org.pdf.takeyourtime.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pdf.takeyourtime.constants.AbsenceConstants;
import org.pdf.takeyourtime.constants.UserConstants;
import org.pdf.takeyourtime.dto.AbsenceDTO;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions.AbsenceAlreadyApprovedException;
import org.pdf.takeyourtime.exceptions.AbsenceExceptions.StandInUserIsUserException;
import org.pdf.takeyourtime.exceptions.UserExceptions.DoesNotExistException;
import org.pdf.takeyourtime.models.Absence;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.models.UserRole;
import org.pdf.takeyourtime.services.AbsenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class AbsenceController {

  private final AbsenceService absenceService;

  public AbsenceController(AbsenceService absenceService) {
    this.absenceService = absenceService;
  }

  // todo: standInUser not change when approved
  @PostMapping("/absence")
  public ResponseEntity<?> addOrEditAbsence(@RequestBody AbsenceDTO newAbsence,
      @AuthenticationPrincipal User requestUser) {
    try {
      Optional<Absence> optionalAbsence = absenceService.addOrEditAbsence(absenceService.absenceFromDTO(newAbsence),
          requestUser.getId());

      if (optionalAbsence.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }

      return ResponseEntity.status(HttpStatus.CREATED).body(optionalAbsence.get().toDTO());
    } catch (AbsenceExceptions.UserNotFound | DoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserConstants.NOT_FOUND);
    } catch (AbsenceExceptions.StandInUserNotFound e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AbsenceConstants.STANDIN_USER_NOT_FOUND);
    } catch (AbsenceExceptions.NotAllowedToAddOrEdit e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AbsenceConstants.NOT_ALLOWED_TO_ADD_OR_EDIT);
    } catch (AbsenceExceptions.NotEnoughVacationDays e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.NOT_ENOUGH_VACATION_DAYS);
    } catch (AbsenceExceptions.NotEnoughSpecialLeaveDays e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.NOT_ENOUGH_SPECIAL_LEAVE_DAYS);
    } catch (AbsenceExceptions.AlreadyAbsentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.ALREADY_ABSENT);
    } catch (AbsenceExceptions.StandInUserIsAbsentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.STANDIN_USER_IS_ABSENT);
    } catch (AbsenceExceptions.DoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AbsenceConstants.NOT_FOUND);
    } catch (StandInUserIsUserException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.STANDIN_USER_IS_USER);
    } catch (AbsenceAlreadyApprovedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.ALREADY_APPROVED_EDIT);
    }
  }

  @GetMapping("/absences")
  public ResponseEntity<List<AbsenceDTO>> getAllAbsences() {
    return ResponseEntity.ok(absenceService.getAllAbsences().stream()
        .map(Absence::toDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping("/absences/mydepartment")
  public ResponseEntity<List<AbsenceDTO>> getAllAbsencesFromMyDepartment(@AuthenticationPrincipal User requestUser) {
    if (requestUser.getSupervisingDepartment() == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    return ResponseEntity.ok(absenceService.getAllAbsences().stream()
        .filter(absence -> absence.getUser().getDepartment().getId() == requestUser.getSupervisingDepartment().getId())
        .map(Absence::toDTO)
        .collect(Collectors.toList()));
  }

  @DeleteMapping("/absence/{id}")
  public ResponseEntity<?> removeAbsenceById(@PathVariable("id") Long id, @AuthenticationPrincipal User requestUser) {
    try {
      Absence absence = absenceService.getAbsenceById(id).orElseThrow(AbsenceExceptions.NotFoundException::new);

      if (!(requestUser.getUserRole() == UserRole.ADMIN && absence.getUser().getId() == absence.getUser().getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AbsenceConstants.NOT_ALLOWED_TO_DELETE);
      }

      boolean isRemoved = absenceService.removeAbsenceById(id);

      if (isRemoved) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
      }

      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AbsenceConstants.NO_ABSENCE_ID_PROVIDED);
    } catch (AbsenceExceptions.NotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AbsenceConstants.NOT_FOUND);
    } catch (AbsenceAlreadyApprovedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(AbsenceConstants.ALREADY_APPROVED);
    }
  }

  // todo: check if standInUser is existing
  @PostMapping("/absence/{id}/approve")
  public ResponseEntity<Absence> approveAbsence(@PathVariable("id") Long id,
      @AuthenticationPrincipal User requestUser) {
    Optional<Absence> optionalAbsence = absenceService.getAbsenceById(id);

    if (optionalAbsence.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    optionalAbsence = absenceService.approveAbsenceIfSupervisor(optionalAbsence.get(), requestUser.getId());

    if (optionalAbsence.isEmpty()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(optionalAbsence.get());
  }
}
