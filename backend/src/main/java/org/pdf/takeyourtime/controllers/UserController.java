package org.pdf.takeyourtime.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pdf.takeyourtime.constants.DepartmentsConstants;
import org.pdf.takeyourtime.constants.UserConstants;
import org.pdf.takeyourtime.dto.UserDTO;
import org.pdf.takeyourtime.exceptions.DepartmentExceptions;
import org.pdf.takeyourtime.exceptions.UserExceptions;
import org.pdf.takeyourtime.exceptions.UserExceptions.DoesNotExistException;
import org.pdf.takeyourtime.exceptions.UserExceptions.UsernameAlreadyExistsException;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.models.UserRole;
import org.pdf.takeyourtime.services.UserService;
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
@RequestMapping("api/v1/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/all")
  public ResponseEntity<List<UserDTO>> getAllUsers(@AuthenticationPrincipal User requestUser) {
    if (requestUser.getUserRole() == UserRole.ADMIN) {
      return ResponseEntity.ok(userService.getAll()
          .stream()
          .map(User::toDTO)
          .collect(Collectors.toList()));
    }

    if (requestUser.getSupervisingDepartment() != null) {
      return ResponseEntity.ok(requestUser.getDepartment().getMembers()
          .stream()
          .map(User::toDTO)
          .collect(Collectors.toList()));
    }

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
  }

  @GetMapping
  public ResponseEntity<UserDTO> getOwnUser(@AuthenticationPrincipal User requestUser) {
    return ResponseEntity.ok(requestUser.toDTO());
  }

  @GetMapping("/stats")
  public ResponseEntity<?> getUserStats(@AuthenticationPrincipal User requestUser) {
    return ResponseEntity.ok(userService.getStatsDTO(requestUser));
  }

  @PostMapping
  public ResponseEntity<?> addOrEditUser(@RequestBody UserDTO newUserDetails,
      @AuthenticationPrincipal User requestUser) {
    try {
      Optional<User> optionalUser = Optional.empty();

      if (newUserDetails.id() != null) {
        optionalUser = userService.getUserById(newUserDetails.id());
      }

      if (optionalUser.isEmpty() && !requestUser.isAdmin()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_PERMITTED);
      }

      if (!requestUser.isAdmin() && newUserDetails.userRole() != UserRole.USER) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_PERMITTED_TO_CHANGE_ROLE);
      }

      if (optionalUser.isPresent() && optionalUser.get().getUserRole() == UserRole.ADMIN
          && newUserDetails.userRole() != UserRole.ADMIN && userService.getAmountAdmins() < 2L) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_ENOUGH_ADMINS);
      }

      if (optionalUser.isPresent() && !(requestUser.getId() == optionalUser.get().getId() || requestUser.isAdmin())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_PERMITTED);
      }

      if (optionalUser.isPresent() && newUserDetails.departmentId() != null
          && newUserDetails.departmentId() != optionalUser.get().getDepartment().getId() && !requestUser.isAdmin()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_PERMITTED_CHANGE_DEPARTMENT);
      }

      User user = userService.addOrEditUser(newUserDetails);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
      }

      return ResponseEntity.ok(user.toDTO());
    } catch (UserExceptions.DTODepartmentIdIsNullException | DepartmentExceptions.DepartmentDoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(DepartmentsConstants.NOT_FOUND);
    } catch (DepartmentExceptions.SupervisingDepartmentDoesNotMatchException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(DepartmentsConstants.SUPERVISING_NOT_MATCH);
    } catch (UsernameAlreadyExistsException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserConstants.USERNAME_ALREADY_EXISTS);
    } catch (DoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserConstants.NOT_FOUND);
    }
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<?> removeUser(@PathVariable("userId") Long userId, @AuthenticationPrincipal User requestUser) {
    if (requestUser.getId() != userId && requestUser.getUserRole() != UserRole.ADMIN) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(UserConstants.NOT_PERMITTED);
    }

    try {
      boolean isRemoved = userService.removeById(userId);

      if (isRemoved) {
        return ResponseEntity.ok(true);
      }

      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    } catch (UserExceptions.DoesNotExistException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserConstants.NOT_FOUND);
    } catch (UserExceptions.UserIsSupervisorException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(UserConstants.CANNOT_DELETE_SUPERVISOR);
    } catch (UserExceptions.NotEnoughAdminsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(UserConstants.NOT_ENOUGH_ADMINS);
    }
  }
}
