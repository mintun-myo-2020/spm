package com.eggtive.spm.testpaper.repository;

import com.eggtive.spm.testpaper.entity.TestPaperPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TestPaperPageRepository extends JpaRepository<TestPaperPage, UUID> {
}
