package org.pdf.takeyourtime.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.pdf.takeyourtime.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column
  private boolean isDisabled;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole userRole;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @ManyToOne
  private Department department;

  @OneToOne
  private Department supervisingDepartment;

  @OneToMany(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<Absence> absences;

  @OneToMany(mappedBy = "standInUser", cascade = { CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER)
  private Set<Absence> standInAbsences;

  @Column
  private int vacationDays;

  @Column
  private int specialLeaveDays;

  public User() {
    this.isDisabled = false;
    this.absences = Set.of();
    this.standInAbsences = Set.of();
    this.vacationDays = 30;
    this.specialLeaveDays = 2;
  }

  public User(UserRole userRole, String username, String password) {
    this();
    this.userRole = userRole;
    this.username = username;
    this.password = password;
  }

  public User(UserRole userRole, String username, String password, Department department) {
    this(userRole, username, password);
    this.department = department;
    this.supervisingDepartment = null;
  }

  public User(UserRole userRole, String username, String password, Department department, boolean isSupervisor) {
    this(userRole, username, password, department);
    this.supervisingDepartment = isSupervisor ? department : null;
  }

  /**
   * Creates a {@link User} with the default {@link UserRole#USER} role.
   */
  public User(String username, String password) {
    this(UserRole.USER, username, password);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    SimpleGrantedAuthority auth = new SimpleGrantedAuthority(userRole.name());
    return Collections.singleton(auth);
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !isDisabled;
  }

  public UserDTO toDTO() {
    return userToUserDTO(this);
  }

  public static UserDTO userToUserDTO(User user) {
    return new UserDTO(user.id, user.isDisabled, user.userRole, user.username, null, user.department.getId(),
        user.supervisingDepartment != null ? user.supervisingDepartment.getId() : null, user.vacationDays, user.specialLeaveDays);
  }

  public Long getId() {
    return id;
  }

  public UserRole getUserRole() {
    return userRole;
  }

  public boolean isAdmin() {
    return userRole == UserRole.ADMIN;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public Set<Absence> getAbsences() {
    return absences;
  }

  public boolean removeAbsence(Absence absence) {
    return absences.remove(absence);
  }

  public Set<Absence> getStandInAbsences() {
    return standInAbsences;
  }

  public boolean removeStandInAbsence(Absence absence) {
    return standInAbsences.remove(absence);
  }

  public Department getSupervisingDepartment() {
    return supervisingDepartment;
  }

  public void setSupervisingDepartment(Department departmentEntity) {
    this.supervisingDepartment = departmentEntity;
  }

  public void setUserRole(UserRole userRole) {
    this.userRole = userRole;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setEncryptedPassword(String password) {
    this.password = password;
  }

  public int getVacationDays() {
    return vacationDays;
  }

  public void setVacationDays(int vacationDays) {
    this.vacationDays = vacationDays;
  }

  public int getSpecialLeaveDays() {
    return specialLeaveDays;
  }

  public void setSpecialLeaveDays(int specialLeaveDays) {
    this.specialLeaveDays = specialLeaveDays;
  }
}
