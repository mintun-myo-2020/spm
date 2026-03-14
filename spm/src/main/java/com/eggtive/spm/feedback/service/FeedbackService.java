package com.eggtive.spm.feedback.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.FeedbackCategory;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.feedback.dto.*;
import com.eggtive.spm.feedback.entity.Feedback;
import com.eggtive.spm.feedback.entity.FeedbackTemplate;
import com.eggtive.spm.feedback.repository.FeedbackRepository;
import com.eggtive.spm.feedback.repository.FeedbackTemplateRepository;
import com.eggtive.spm.testscore.entity.TestScore;
import com.eggtive.spm.testscore.service.TestScoreService;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackTemplateRepository templateRepository;
    private final TestScoreService testScoreService;

    public FeedbackService(FeedbackRepository fbRepo, FeedbackTemplateRepository tplRepo,
                           TestScoreService testScoreService) {
        this.feedbackRepository = fbRepo;
        this.templateRepository = tplRepo;
        this.testScoreService = testScoreService;
    }

    public FeedbackDTO createFeedback(UUID testScoreId, CreateFeedbackRequestDTO req,
                                       User currentUser, Teacher teacher) {
        TestScore ts = testScoreService.findTestScoreOrThrow(testScoreId);

        Feedback fb = new Feedback();
        fb.setTestScore(ts);
        fb.setTeacher(teacher);
        fb.setStudent(ts.getStudent());
        fb.setStrengths(req.strengths());
        fb.setAreasForImprovement(req.areasForImprovement());
        fb.setRecommendations(req.recommendations());
        fb.setAdditionalNotes(req.additionalNotes());
        fb.setCreatedBy(currentUser);
        fb.setUpdatedBy(currentUser);
        return toDTO(feedbackRepository.save(fb));
    }

    public FeedbackDTO updateFeedback(UUID feedbackId, CreateFeedbackRequestDTO req, User currentUser) {
        Feedback fb = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Feedback not found"));
        fb.setStrengths(req.strengths());
        fb.setAreasForImprovement(req.areasForImprovement());
        fb.setRecommendations(req.recommendations());
        fb.setAdditionalNotes(req.additionalNotes());
        fb.setEdited(true);
        fb.setUpdatedBy(currentUser);
        return toDTO(feedbackRepository.save(fb));
    }

    @Transactional(readOnly = true)
    public List<FeedbackTemplateDTO> getTemplates(UUID teacherId, FeedbackCategory category) {
        return templateRepository.findByTeacherOrSystemWide(teacherId, category)
            .stream().map(this::toTemplateDTO).toList();
    }

    public FeedbackTemplateDTO createTemplate(CreateFeedbackTemplateRequestDTO req, Teacher teacher) {
        FeedbackTemplate ft = new FeedbackTemplate();
        ft.setTeacher(teacher);
        ft.setCategory(req.category());
        ft.setTitle(req.title());
        ft.setContent(req.content());
        return toTemplateDTO(templateRepository.save(ft));
    }

    private FeedbackDTO toDTO(Feedback fb) {
        String teacherName = fb.getTeacher().getUser().getFirstName() + " " + fb.getTeacher().getUser().getLastName();
        return new FeedbackDTO(fb.getId(), fb.getTestScore().getId(), fb.getTeacher().getId(),
            teacherName, fb.getStudent().getId(), fb.getStrengths(), fb.getAreasForImprovement(),
            fb.getRecommendations(), fb.getAdditionalNotes(), fb.isEdited(),
            fb.getCreatedAt(), fb.getUpdatedAt());
    }

    private FeedbackTemplateDTO toTemplateDTO(FeedbackTemplate ft) {
        return new FeedbackTemplateDTO(ft.getId(), ft.getCategory().name(), ft.getTitle(),
            ft.getContent(), ft.isSystemWide(),
            ft.getTeacher() != null ? ft.getTeacher().getId() : null);
    }
}

