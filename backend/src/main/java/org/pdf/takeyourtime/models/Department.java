package org.pdf.takeyourtime.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.pdf.takeyourtime.dto.DepartmentDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Department implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "department", cascade = { CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER)
  private Set<User> members;

  @OneToOne(mappedBy = "supervisingDepartment", cascade = { CascadeType.PERSIST,
      CascadeType.REFRESH }, fetch = FetchType.EAGER)
  private User supervisor;

  public Department() {
    this.members = new HashSet<>();
    this.supervisor = null;
  }

  public Department(String name) {
    this();
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public User getSupervisor() {
    return supervisor; // maybe make it unmodifiable?
  }

  public Set<User> getMembers() {
    return Collections.unmodifiableSet(members); // maybe dont make it unmodifiable?
  }

  public boolean isMember(User user) {
    return members.contains(user);
  }

  public void addMember(User member) {
    members.add(member);
  }

  public boolean removeMember(User user) {
    return members.remove(user);
  }

  public DepartmentDTO toDTO() {
    return new DepartmentDTO(id, name,
        members.stream().map(User::toDTO).collect(Collectors.toSet()),
        supervisor != null ? supervisor.getUsername() : "");
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSupervisor(User supervisor) {
    this.supervisor = supervisor;
  }
}
