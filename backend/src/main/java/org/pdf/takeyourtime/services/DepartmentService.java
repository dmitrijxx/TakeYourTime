package org.pdf.takeyourtime.services;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pdf.takeyourtime.dto.DepartmentDTO;
import org.pdf.takeyourtime.exceptions.DepartmentExceptions;
import org.pdf.takeyourtime.models.Department;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.repos.DepartmentRepo;
import org.pdf.takeyourtime.repos.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

  private final DepartmentRepo departmentRepo;
  private final UserService userService;
  private final UserRepo userRepo;

  public DepartmentService(DepartmentRepo departmentRepo, UserService userService, UserRepo userRepo) {
    this.departmentRepo = departmentRepo;
    this.userService = userService;
    this.userRepo = userRepo;
  }

  public Optional<Department> getDepartmentById(Long departmentId) {
    return departmentRepo.findById(departmentId);
  }

  public Optional<Department> getDepartmentByName(String departmentName) {
    return departmentRepo.findByName(departmentName);
  }

  public List<Department> getAllDepartments() {
    return departmentRepo.findAll();
  }

  public Set<User> getAbsentMembers(Department department, LocalDate checkDate) {
    return department.getMembers().stream()
        .filter(member -> userService.isAbsentWhileDate(member, checkDate))
        .collect(Collectors.toSet());
  }

  public Set<User> getAbsentMembers(Department department, LocalDate startCheckDate, LocalDate endCheckDate) {
    Set<User> absentMembers = new HashSet<>();

    for (LocalDate date = startCheckDate; !date.isAfter(endCheckDate); date = date.plusDays(1)) {
      absentMembers.addAll(getAbsentMembers(department, date));
    }

    return absentMembers;
  }

  public Department addEmptyDepartment(String name) throws DepartmentExceptions.DepartmentAlreadyExistsException {
    Optional<Department> departmentOptional = getDepartmentByName(name);

    if (departmentOptional.isPresent()) {
      throw new DepartmentExceptions.DepartmentAlreadyExistsException();
    }

    Department departmentEntity = new Department(name);
    return this.departmentRepo.save(departmentEntity);
  }

  private void validateAddData(String name, String supervisorName)
      throws DepartmentExceptions.NoDepartmentNameGivenException,
      DepartmentExceptions.NoSupervisorGivenException {
    if (name == null) {
      throw new DepartmentExceptions.NoDepartmentNameGivenException();
    }

    if (supervisorName == null) {
      throw new DepartmentExceptions.NoSupervisorGivenException();
    }
  }

  private User getAndCheckSupervisor(String supervisorName, Long departmentId)
      throws DepartmentExceptions.SupervisorNotFoundException,
      DepartmentExceptions.SupervisorIsAlreadySupervisorException {
    try {
      User supervisor = (User) this.userService.loadUserByUsername(supervisorName);
      Department supervisingDepartment = supervisor.getSupervisingDepartment();
      if (supervisingDepartment != null && supervisingDepartment.getId() != null
          && supervisingDepartment.getId() != departmentId) {
        throw new DepartmentExceptions.SupervisorIsAlreadySupervisorException();
      }

      return supervisor;
    } catch (UsernameNotFoundException e) {
      throw new DepartmentExceptions.SupervisorNotFoundException();
    }
  }

  public Department addDepartmentWithSupervisor(String name, String supervisorName)
      throws DepartmentExceptions.NoDepartmentNameGivenException, DepartmentExceptions.NoSupervisorGivenException,
      DepartmentExceptions.DepartmentAlreadyExistsException,
      DepartmentExceptions.SupervisorNotFoundException,
      DepartmentExceptions.SupervisorIsAlreadySupervisorException {
    validateAddData(name, supervisorName);

    Optional<Department> departmentOptional = getDepartmentByName(name);
    if (departmentOptional.isPresent()) {
      throw new DepartmentExceptions.DepartmentAlreadyExistsException();
    }

    User supervisor = getAndCheckSupervisor(supervisorName, null);
    Department departmentEntity = new Department(name);

    departmentEntity.addMember(supervisor);
    departmentEntity.setSupervisor(supervisor);

    supervisor.setDepartment(departmentEntity);
    supervisor.setSupervisingDepartment(departmentEntity);

    return this.departmentRepo.save(departmentEntity);
  }

  private void validateEditDepartmentDTO(DepartmentDTO department)
      throws DepartmentExceptions.NoSupervisorGivenException, IllegalArgumentException {
    if (department != null && department.supervisorName() == null) {
      throw new DepartmentExceptions.NoSupervisorGivenException();
    }

    if (department == null ||
        department.id() == null ||
        department.name() == null ||
        department.supervisorName() == null) {
      throw new IllegalArgumentException();
    }
  }

  private Department getAndCheckDepartment(DepartmentDTO department)
      throws DepartmentExceptions.DepartmentDoesNotExistException {
    Optional<Department> departmentOptional = getDepartmentById(department.id());
    if (departmentOptional.isEmpty()) {
      throw new DepartmentExceptions.DepartmentDoesNotExistException();
    }

    return departmentOptional.get();
  }

  public Department editDepartmentFromDTO(DepartmentDTO department)
      throws DepartmentExceptions.NoSupervisorGivenException, IllegalArgumentException,
      DepartmentExceptions.DepartmentDoesNotExistException,
      DepartmentExceptions.SupervisorNotFoundException,
      DepartmentExceptions.SupervisorIsAlreadySupervisorException,
      UsernameNotFoundException {
    validateEditDepartmentDTO(department);

    User newSupervisor = getAndCheckSupervisor(department.supervisorName(), department.id());
    Department departmentEntity = getAndCheckDepartment(department);

    // first edit old supervisor
    String oldSupervisorName = departmentEntity.getSupervisor().getUsername();
    User oldSupervisor = (User) this.userService.loadUserByUsername(oldSupervisorName);
    oldSupervisor.setSupervisingDepartment(null);
    this.userRepo.save(oldSupervisor);

    // now set new supervisor
    departmentEntity.setName(department.name());
    departmentEntity.setSupervisor(newSupervisor);
    newSupervisor.setSupervisingDepartment(departmentEntity);

    if (!departmentEntity.isMember(newSupervisor)) {
      departmentEntity.addMember(newSupervisor);
      newSupervisor.setDepartment(departmentEntity);
    }

    return this.departmentRepo.save(departmentEntity);
  }

  public boolean removeDepartmentById(Long deleteDepartmentId, Long replaceDepartment)
      throws DepartmentExceptions.DepartmentDoesNotExistException,
      DepartmentExceptions.ReplaceDepartmentDoesNotExistException {
    if (deleteDepartmentId == null || getDepartmentById(deleteDepartmentId).isEmpty()) {
      throw new DepartmentExceptions.DepartmentDoesNotExistException();
    }

    Department deleteDepartmentEntity = getDepartmentById(deleteDepartmentId)
        .orElseThrow(DepartmentExceptions.DepartmentDoesNotExistException::new);
    Department replaceDepartmentEntity = getDepartmentById(replaceDepartment)
        .orElseThrow(DepartmentExceptions.ReplaceDepartmentDoesNotExistException::new);

    Set<User> members = deleteDepartmentEntity.getMembers();
    members.forEach(member -> {
      if (member.getSupervisingDepartment() != null
          && member.getSupervisingDepartment().getId().equals(deleteDepartmentId)) {
        member.setSupervisingDepartment(null);
      }
      
      replaceDepartmentEntity.addMember(member);
      member.setDepartment(replaceDepartmentEntity);

      departmentRepo.save(replaceDepartmentEntity);
      userRepo.save(member);
    });

    this.departmentRepo.deleteById(deleteDepartmentId);
    return getDepartmentById(deleteDepartmentId).isEmpty();
  }

  public boolean doesSupervisorChange(DepartmentDTO department)
      throws DepartmentExceptions.NoSupervisorGivenException, IllegalArgumentException,
      DepartmentExceptions.DepartmentDoesNotExistException {
    validateEditDepartmentDTO(department);
    Department departmentEntity = getAndCheckDepartment(department);

    return !department.supervisorName().equals(departmentEntity.getSupervisor().getUsername());
  }
}
