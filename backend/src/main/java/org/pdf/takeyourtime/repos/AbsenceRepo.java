package org.pdf.takeyourtime.repos;

import java.time.LocalDate;
import java.util.List;

import org.pdf.takeyourtime.models.Absence;
import org.pdf.takeyourtime.models.AbsenceType;
import org.pdf.takeyourtime.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = false)
public interface AbsenceRepo extends JpaRepository<Absence, Long> {
    @Query("SELECT a FROM Absence a WHERE a.startDate >= :startDate AND a.endDate <= :endDate AND a.user = :user AND a.absenceType = :absenceType")
    public List<Absence> getAbsencesBetweenDatesByUserAndAbsenceType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("user") User user,
            @Param("absenceType") AbsenceType absenceType);
}
