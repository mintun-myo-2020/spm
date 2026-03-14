package com.eggtive.spm.testscore.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.subject.repository.TopicRepository;
import com.eggtive.spm.testscore.dto.CreateTestScoreRequestDTO;
import com.eggtive.spm.testscore.dto.TestScoreDTO;
import com.eggtive.spm.testscore.entity.Question;
import com.eggtive.spm.testscore.entity.SubQuestion;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.repository.TestScoreRepository;
import com.eggtive.spm.classmanagement.repository.TuitionClassRepository;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class TestScoreService {

    private final TestScoreRepository testScoreRepository;
    private final StudentRepository studentRepository;
    private final TuitionClassRepository classRepository;
    private final TopicRepository topicRepository;

    public TestScoreService(TestScoreRepository tsRepo, StudentRepository studentRepo,
                            TuitionClassRepository classRepo, TopicRepository topicRepo) {
        this.testScoreRepository = tsRepo;
        this.studentRepository = studentRepo;
        this.classRepository = classRepo;
        this.topicRepository = topicRepo;
    }

    public TestScoreDTO createTestScore(CreateTestScoreRequestDTO req, User currentUser, Teacher teacher) {
        Student student = studentRepository.findById(req.studentId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student not found"));
        var tuitionClass = classRepository.findById(req.classId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Class not found"));

        if (req.overallScore().compareTo(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00")) > 0) {
            throw new AppException(ErrorCode.INVALID_SCORE, "Overall score cannot exceed max score");
        }

        TestScore ts = new TestScore();
        ts.setStudent(student);
        ts.setTuitionClass(tuitionClass);
        ts.setTeacher(teacher);
        ts.setTestName(req.testName());
        ts.setTestDate(req.testDate());
        ts.setOverallScore(req.overallScore());
        ts.setMaxScore(req.maxScore() != null ? req.maxScore() : new BigDecimal("100.00"));
        ts.setCreatedBy(currentUser);
        ts.setUpdatedBy(currentUser);

        if (req.questions() != null) {
            for (var qReq : req.questions()) {
                Question q = new Question();
                q.setTestScore(ts);
                q.setQuestionNumber(qReq.questionNumber());
                q.setMaxScore(qReq.maxScore());
                ts.getQuestions().add(q);

                if (qReq.subQuestions() != null) {
                    for (var sqReq : qReq.subQuestions()) {
                        Topic topic = topicRepository.findById(sqReq.topicId())
                            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Topic not found"));
                        SubQuestion sq = new SubQuestion();
                        sq.setQuestion(q);
                        sq.setSubQuestionLabel(sqReq.label());
                        sq.setScore(sqReq.score());
                        sq.setMaxScore(sqReq.maxScore());
                        sq.setTopic(topic);
                        q.getSubQuestions().add(sq);
                    }
                }
            }
        }

        ts = testScoreRepository.save(ts);
        return toDTO(ts);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TestScoreDTO> getStudentTestScores(UUID studentId, LocalDate startDate,
                                                             LocalDate endDate, UUID classId, Pageable pageable) {
        Page<TestScore> page = testScoreRepository.findByStudentWithFilters(studentId, startDate, endDate, classId, pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toDTO).toList());
    }

    @Transactional(readOnly = true)
    public TestScoreDTO getTestScore(UUID testScoreId) {
        TestScore ts = testScoreRepository.findById(testScoreId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Test score not found"));
        return toDTO(ts);
    }

    public void deleteTestScore(UUID testScoreId) {
        if (!testScoreRepository.existsById(testScoreId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Test score not found");
        }
        testScoreRepository.deleteById(testScoreId);
    }

    private TestScoreDTO toDTO(TestScore ts) {
        var questions = ts.getQuestions().stream().map(q -> {
            var subs = q.getSubQuestions().stream().map(sq ->
                new TestScoreDTO.SubQuestionDTO(sq.getId(), sq.getSubQuestionLabel(), sq.getScore(),
                    sq.getMaxScore(), sq.getTopic().getId(), sq.getTopic().getName())
            ).toList();
            return new TestScoreDTO.QuestionDTO(q.getId(), q.getQuestionNumber(), q.getMaxScore(), subs);
        }).toList();

        Student s = ts.getStudent();
        String studentName = s.getUser().getFirstName() + " " + s.getUser().getLastName();
        Teacher t = ts.getTeacher();
        String teacherName = t.getUser().getFirstName() + " " + t.getUser().getLastName();

        return new TestScoreDTO(ts.getId(), s.getId(), studentName,
            ts.getTuitionClass().getId(), ts.getTuitionClass().getName(),
            t.getId(), teacherName, ts.getTestName(), ts.getTestDate(),
            ts.getOverallScore(), ts.getMaxScore(), questions,
            ts.getCreatedAt(), ts.getUpdatedAt());
    }
}
