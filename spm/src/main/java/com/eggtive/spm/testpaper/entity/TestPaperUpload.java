package com.eggtive.spm.testpaper.entity;

import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.testpaper.enums.UploadStatus;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_paper_uploads")
public class TestPaperUpload extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_score_id")
    private TestScore testScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private TuitionClass tuitionClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private UploadStatus status = UploadStatus.UPLOADED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @OneToMany(mappedBy = "upload", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<TestPaperPage> pages = new ArrayList<>();

    public TestScore getTestScore() { return testScore; }
    public void setTestScore(TestScore testScore) { this.testScore = testScore; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public TuitionClass getTuitionClass() { return tuitionClass; }
    public void setTuitionClass(TuitionClass tuitionClass) { this.tuitionClass = tuitionClass; }
    public UploadStatus getStatus() { return status; }
    public void setStatus(UploadStatus status) { this.status = status; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public List<TestPaperPage> getPages() { return pages; }
}
