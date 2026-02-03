package org.pdf.takeyourtime.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pdf.takeyourtime.constants.LoginConstants;
import org.pdf.takeyourtime.dto.StatsDTO;
import org.pdf.takeyourtime.dto.UserDTO;
import org.pdf.takeyourtime.exceptions.DepartmentExceptions;
import org.pdf.takeyourtime.exceptions.UserExceptions;
import org.pdf.takeyourtime.models.Absence;
import org.pdf.takeyourtime.models.AbsenceType;
import org.pdf.takeyourtime.models.Department;
import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.models.UserRole;
import org.pdf.takeyourtime.repos.AbsenceRepo;
import org.pdf.takeyourtime.repos.DepartmentRepo;
import org.pdf.takeyourtime.repos.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService implements UserDetailsService {

  private final UserRepo userRepo;
  private final DepartmentRepo departmentRepo;
  private final AbsenceRepo absenceRepo;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepo userRepo, DepartmentRepo departmentRepo, AbsenceRepo absenceRepo,
      PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.departmentRepo = departmentRepo;
    this.absenceRepo = absenceRepo;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final Optional<User> user = userRepo.findByUsername(username);
    if (user.isEmpty()) {
      throw new UsernameNotFoundException(LoginConstants.USER_NOT_FOUND);
    }

    return user.get();
  }

  public Optional<User> getUserById(Long userId) {
    return userRepo.findById(userId);
  }

  public Optional<User> getUserFromDTO(UserDTO user) {
    return getUserById(user.id());
  }

  public boolean isAbsentWhileDate(User user, LocalDate checkDate) {
    return user.getAbsences().stream()
        .filter(Absence::isApproved)
        .anyMatch(absence -> absence.isWhileDate(checkDate));
  }

  public boolean isAbsentWhileDate(User user, LocalDate startCheckDate, LocalDate endCheckDate) {
    for (LocalDate checkDate = startCheckDate; !checkDate.isAfter(endCheckDate); checkDate = checkDate.plusDays(1)) {
      boolean isAbsentOnCheckDate = isAbsentWhileDate(user, checkDate);
      if (isAbsentOnCheckDate)
        return true;
    }

    return false;
  }

  private List<LocalDate> getHolidays() {
    List<LocalDate> holidays = new ArrayList<>();

    try {
      RestTemplate restTemplate = new RestTemplate();
      String response = restTemplate.getForObject("https://get.api-feiertage.de/", String.class);

      JSONObject jsonResponse = new JSONObject(response);
      JSONArray holidaysArray = jsonResponse.getJSONArray("feiertage");
      for (int i = 0; i < holidaysArray.length(); i++) {
        JSONObject holiday = holidaysArray.getJSONObject(i);
        LocalDate holidayDate = LocalDate.parse(holiday.getString("date"));
        holidays.add(holidayDate);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return holidays;
  }

  public int getDuration(Absence absence) {
    int duration = 0;
    List<LocalDate> holidays = getHolidays(); // todo: could be cached

    for (LocalDate checkDate = absence.getStartDate(); !checkDate.isAfter(absence.getEndDate()); checkDate = checkDate
        .plusDays(1)) {
      if (!(checkDate.getDayOfWeek() == DayOfWeek.SATURDAY || checkDate.getDayOfWeek() == DayOfWeek.SUNDAY
          || holidays.contains(checkDate))) {
        duration++;
      }
    }

    return duration;
  }

  private int calculateTotalAbsenceDaysByType(User user, AbsenceType absenceType) {
    int currentYear = LocalDate.now().getYear();
    LocalDate currentYearStart = LocalDate.of(currentYear, 1, 1);
    LocalDate currentYearEnd = LocalDate.of(currentYear, 12, 31);

    return absenceRepo.getAbsencesBetweenDatesByUserAndAbsenceType(currentYearStart, currentYearEnd, user, absenceType)
        .stream()
        .mapToInt(this::getDuration)
        .sum();
  }

  public int getCommulatedSpecialLeaveDays(User user) throws UserExceptions.DoesNotExistException {
    User absentUser = getUserById(user.getId()).orElseThrow(UserExceptions.DoesNotExistException::new);

    return absentUser.getSpecialLeaveDays() - calculateTotalAbsenceDaysByType(absentUser, AbsenceType.SPECIAL_LEAVE);
  }

  public int getCommulatedVacationDays(User user) throws UserExceptions.DoesNotExistException {
    User absentUser = getUserById(user.getId()).orElseThrow(UserExceptions.DoesNotExistException::new);

    return absentUser.getVacationDays() - calculateTotalAbsenceDaysByType(absentUser, AbsenceType.VACATION);
  }

  public List<User> getAll() {
    return userRepo.findAll();
  }

  public User addOrEditUser(UserDTO newUserDetails)
      throws UserExceptions.DTODepartmentIdIsNullException, DepartmentExceptions.DepartmentDoesNotExistException,
      DepartmentExceptions.SupervisingDepartmentDoesNotMatchException, UserExceptions.UsernameAlreadyExistsException,
      UserExceptions.DoesNotExistException {
    if (newUserDetails.departmentId() == null) {
      throw new UserExceptions.DTODepartmentIdIsNullException();
    }

    Optional<Department> optionalDepartment = departmentRepo.findById(newUserDetails.departmentId());
    if (optionalDepartment.isEmpty()) {
      throw new DepartmentExceptions.DepartmentDoesNotExistException();
    }
    Department department = optionalDepartment.get();

    Department supervisingDepartment = null;
    if (newUserDetails.supervisingDepartmentId() != null) {
      if (newUserDetails.supervisingDepartmentId() != department.getId()) {
        throw new DepartmentExceptions.SupervisingDepartmentDoesNotMatchException();
      }

      supervisingDepartment = department;
    }

    User adddedOrEditedUser = null;
    if (newUserDetails.id() != null) {
      Optional<User> optionalUser = getUserById(newUserDetails.id());

      if (optionalUser.isEmpty()) {
        throw new UserExceptions.DoesNotExistException();
      }

      adddedOrEditedUser = optionalUser.get();
    }

    Optional<User> userWithSameUsername = userRepo.findByUsername(newUserDetails.username());
    if (userWithSameUsername.isPresent() && userWithSameUsername.get().getId() != newUserDetails.id()) {
      throw new UserExceptions.UsernameAlreadyExistsException();
    }

    if (adddedOrEditedUser == null) {
      adddedOrEditedUser = new User();
    }

    adddedOrEditedUser.setUserRole(newUserDetails.userRole());
    adddedOrEditedUser.setUsername(newUserDetails.username());
    if (newUserDetails.password() != null) {
      adddedOrEditedUser.setEncryptedPassword(passwordEncoder.encode(newUserDetails.password()));
    }
    adddedOrEditedUser.setDepartment(department);
    adddedOrEditedUser.setSupervisingDepartment(supervisingDepartment);

    department.addMember(adddedOrEditedUser);
    return userRepo.save(adddedOrEditedUser);
  }

  public User createDefaultUser(long defaultDepartmentId, String defaultAdminUsername, String defaultAdminPassword)
      throws DepartmentExceptions.DepartmentDoesNotExistException, UserExceptions.DTODepartmentIdIsNullException,
      DepartmentExceptions.SupervisingDepartmentDoesNotMatchException, UserExceptions.UsernameAlreadyExistsException,
      UserExceptions.DoesNotExistException {
    Optional<Department> department = departmentRepo.findById(defaultDepartmentId);

    if (department.isEmpty()) {
      throw new DepartmentExceptions.DepartmentDoesNotExistException();
    }

    UserDTO defaultUser = new UserDTO(null, false, UserRole.ADMIN, defaultAdminUsername, defaultAdminPassword,
        department.get().getId(), department.get().getId(), 30, 2);

    return addOrEditUser(defaultUser);
  }

  public long getAmountAdmins() {
    return userRepo.countAllByUserRole(UserRole.ADMIN);
  }

  public boolean removeById(Long userId)
      throws UserExceptions.DoesNotExistException, UserExceptions.UserIsSupervisorException,
      UserExceptions.NotEnoughAdminsException {
    User user = getUserById(userId).orElseThrow(UserExceptions.DoesNotExistException::new);
    Department department = user.getDepartment();

    if (user.getUserRole() == UserRole.ADMIN && getAmountAdmins() < 2L) {
      throw new UserExceptions.NotEnoughAdminsException();
    }

    if (user.getSupervisingDepartment() != null) {
      throw new UserExceptions.UserIsSupervisorException();
    }

    user.getStandInAbsences().forEach(absence -> {
      absence.setStandInUser(null);
      absenceRepo.save(absence);
    });

    user.getAbsences().forEach(absence -> {
      User standInUser = absence.getStandInUser();

      if (standInUser != null) {
        standInUser.removeStandInAbsence(absence);
        userRepo.save(standInUser);
      }

      user.removeAbsence(absence);
      userRepo.save(user);
      absenceRepo.deleteById(absence.getId());
    });

    department.removeMember(user);
    departmentRepo.save(department);

    userRepo.deleteById(userId);
    return getUserById(userId).isEmpty();
  }

  public StatsDTO getStatsDTO(User user) {
    return new StatsDTO(calculateTotalAbsenceDaysByType(user, AbsenceType.SICK_LEAVE),
        calculateTotalAbsenceDaysByType(user, AbsenceType.UNPAID_LEAVE), user.getVacationDays(),
        calculateTotalAbsenceDaysByType(user, AbsenceType.VACATION), user.getSpecialLeaveDays(),
        calculateTotalAbsenceDaysByType(user, AbsenceType.SPECIAL_LEAVE));
  }
}
