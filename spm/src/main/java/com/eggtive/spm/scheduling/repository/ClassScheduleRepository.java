package com.eggtive.spm.scheduling.repository;

import com.eggtive.spm.scheduling.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID> {

    List<ClassSchedule> findByTuitionClassId(UUID classId);

    @Query("SELECT s FROM ClassSchedule s WHERE s.tuitionClass.id = :classId AND (s.effectiveUntil IS NULL OR s.effectiveUntil >= CURRENT_DATE)")
    List<ClassSchedule> findActiveByClassId(UUID classId);

    @Query("SELECT COUNT(s) > 0 FROM ClassSchedule s WHERE s.tuitionClass.id = :classId AND s.dayOfWeek = :dayOfWeek AND s.isRecurring = true AND (s.effectiveUntil IS NULL OR s.effectiveUntil >= CURRENT_DATE)")
    boolean existsActiveRecurringByClassIdAndDayOfWeek(UUID classId, int dayOfWeek);
}
