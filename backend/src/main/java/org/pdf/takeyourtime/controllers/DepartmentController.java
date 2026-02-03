package org.pdf.takeyourtime.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pdf.takeyourtime.dto.UserDTO;
import org.pdf.takeyourtime.exceptions.DepartmentExceptions;
import org.pdf.takeyourtime.constants.DepartmentsConstants;
import org.pdf.takeyourtime.dto.DepartmentDTO;
import org.pdf.takeyourtime.models.Department;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.services.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/department")
public class DepartmentController {

  private DepartmentService departmentService;

  public DepartmentController(DepartmentService departmentService) {
    this.departmentService = departmentService;
  }

  @GetMapping("/all")
  public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
    return ResponseEntity.ok(this.departmentService.getAllDepartments()
        .stream()
        .map(Department::toDTO)
        .collect(Collectors.toList()));
  }

  @GetMapping("/members")
  public ResponseEntity<List<UserDTO>> getOwnDepartmentMembers(@AuthenticationPrincipal User requestUser) {
    Department department = requestUser.getDepartment();

    if (department == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    return ResponseEntity.ok(department.getMembers()
        .stream()
        .map(User::toDTO)
        .collect(Collectors.toList()));
  }

  private ResponseEntity<?> internalAddDepartment(DepartmentDTO department, User requestUser)
      throws DepartmentExceptions.NoDepartmentNameGivenException, DepartmentExceptions.NoSupervisorGivenException,
      DepartmentExceptions.SupervisorNotFoundException,
      DepartmentExceptions.SupervisorIsAlreadySupervisorException {
    if (!requestUser.isAdmin()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    try {
      final Department editedDepartment = this.departmentService.addDepartmentWithSupervisor(
          department.name(),
          department.supervisorName());

      return ResponseEntity.status(HttpStatus.CREATED).body(editedDepartment.toDTO());
    } catch (DepartmentExceptions.DepartmentAlreadyExistsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.ALREADY_EXISTS);
    }
  }

  private ResponseEntity<?> internalEditDepartment(DepartmentDTO department, User requestUser)
      throws DepartmentExceptions.NoSupervisorGivenException, IllegalArgumentException,
      DepartmentExceptions.SupervisorNotFoundException,
      DepartmentExceptions.SupervisorIsAlreadySupervisorException,
      UsernameNotFoundException {
    try {
      if (!requestUser.isAdmin() && !requestUser.getSupervisingDepartment().getId().equals(department.id())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(DepartmentsConstants.NOT_ALLOWED);
      }

      if (!requestUser.isAdmin() && departmentService.doesSupervisorChange(department)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(DepartmentsConstants.NOT_ALLOWED_CHANGE_SUPERVISOR);
      }

      return ResponseEntity.ok(this.departmentService.editDepartmentFromDTO(department).toDTO());
    } catch (DepartmentExceptions.DepartmentDoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  @PostMapping
  public ResponseEntity<?> addOrEditDepartment(@RequestBody DepartmentDTO department,
      @AuthenticationPrincipal User requestUser) {
    try {
      if (department == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
      }

      if (department.id() == null) {
        return internalAddDepartment(department, requestUser);
      }

      return internalEditDepartment(department, requestUser);
    } catch (DepartmentExceptions.NoDepartmentNameGivenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.NO_DEPARTMENT_GIVEN);
    } catch (DepartmentExceptions.NoSupervisorGivenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.NO_SUPERVISOR_GIVEN);
    } catch (DepartmentExceptions.SupervisorNotFoundException | UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.SUPERVISOR_NOT_FOUND);
    } catch (DepartmentExceptions.SupervisorIsAlreadySupervisorException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.IS_ALREADY_SUPERVISOR);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(DepartmentsConstants.NOT_VALID);
    }
  }

  @DeleteMapping("/{departmentId}/{replaceDepartmentId}")
  public ResponseEntity<?> removeDepartment(@PathVariable("departmentId") Long departmentId,
  @PathVariable("replaceDepartmentId") Long replaceDepartmentId, @AuthenticationPrincipal User requestUser) {
    if (!requestUser.isAdmin()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
    }

    try {
      boolean isRemoved = this.departmentService.removeDepartmentById(departmentId, replaceDepartmentId);

      if (isRemoved) {
        return ResponseEntity.ok(true);
      }

      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    } catch (DepartmentExceptions.DepartmentDoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(DepartmentsConstants.NOT_FOUND);
    } catch (DepartmentExceptions.ReplaceDepartmentDoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(DepartmentsConstants.REPLACE_DEPARTMENT_NOT_FOUND);
    }
  }
}