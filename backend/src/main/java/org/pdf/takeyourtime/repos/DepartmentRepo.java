package org.pdf.takeyourtime.repos;

import java.util.Optional;

import org.pdf.takeyourtime.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = false)
public interface DepartmentRepo extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}
