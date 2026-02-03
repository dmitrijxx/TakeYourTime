package org.pdf.takeyourtime.repos;

import java.util.Optional;

import org.pdf.takeyourtime.models.User;
import org.pdf.takeyourtime.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = false)
public interface UserRepo extends JpaRepository<User, Long> {
  public Optional<User> findByUsername(String username);
  public long countAllByUserRole(UserRole userRole);
}
