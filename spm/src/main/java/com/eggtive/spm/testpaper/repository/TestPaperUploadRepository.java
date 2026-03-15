package com.eggtive.spm.testpaper.repository;

import com.eggtive.spm.testpaper.entity.TestPaperUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TestPaperUploadRepository extends JpaRepository<TestPaperUpload, UUID> {

    List<TestPaperUpload> findByStudentId(UUID studentId);

    @Modifying
    @Query("UPDATE TestPaperUpload u SET u.testScore.id = :testScoreId WHERE u.id IN :uploadIds")
    void linkToTestScore(List<UUID> uploadIds, UUID testScoreId);
}
