package com.eggtive.spm.subject.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.subject.dto.*;
import com.eggtive.spm.subject.entity.Subject;
import com.eggtive.spm.subject.entity.Topic;
import com.eggtive.spm.subject.repository.SubjectRepository;
import com.eggtive.spm.subject.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public SubjectService(SubjectRepository subjectRepo, TopicRepository topicRepo) {
        this.subjectRepository = subjectRepo;
        this.topicRepository = topicRepo;
    }

    @Transactional(readOnly = true)
    public List<SubjectDTO> listSubjects(boolean includeInactive) {
        List<Subject> subjects = includeInactive
            ? subjectRepository.findAll()
            : subjectRepository.findByIsActiveTrue();
        return subjects.stream().map(this::toSubjectDTO).toList();
    }

    @Transactional(readOnly = true)
    public SubjectDetailDTO getSubjectDetail(UUID subjectId) {
        Subject s = findOrThrow(subjectId);
        List<TopicDTO> topics = s.getTopics().stream().map(this::toTopicDTO).toList();
        return new SubjectDetailDTO(s.getId(), s.getName(), s.getCode(), s.getDescription(),
            s.isDefault(), s.isActive(), topics);
    }

    public SubjectDTO createSubject(CreateSubjectRequestDTO req) {
        if (subjectRepository.existsByCode(req.code())) {
            throw new AppException(ErrorCode.CONFLICT, "Subject code already exists");
        }
        Subject s = new Subject();
        s.setName(req.name());
        s.setCode(req.code());
        s.setDescription(req.description());
        return toSubjectDTO(subjectRepository.save(s));
    }

    public TopicDTO createTopic(UUID subjectId, CreateTopicRequestDTO req) {
        Subject subject = findOrThrow(subjectId);
        if (topicRepository.existsBySubjectIdAndCode(subjectId, req.code())) {
            throw new AppException(ErrorCode.CONFLICT, "Topic code already exists for this subject");
        }
        Topic t = new Topic();
        t.setSubject(subject);
        t.setName(req.name());
        t.setCode(req.code());
        t.setDescription(req.description());
        return toTopicDTO(topicRepository.save(t));
    }

    public SubjectDTO updateSubject(UUID subjectId, UpdateSubjectRequestDTO req) {
        Subject s = findOrThrow(subjectId);
        s.setName(req.name());
        if (req.description() != null) s.setDescription(req.description());
        return toSubjectDTO(subjectRepository.save(s));
    }

    public TopicDTO updateTopic(UUID topicId, UpdateTopicRequestDTO req) {
        Topic t = topicRepository.findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Topic not found"));
        t.setName(req.name());
        if (req.description() != null) t.setDescription(req.description());
        return toTopicDTO(topicRepository.save(t));
    }

    public SubjectDTO deactivateSubject(UUID subjectId) {
        Subject s = findOrThrow(subjectId);
        s.setActive(false);
        return toSubjectDTO(subjectRepository.save(s));
    }

    public TopicDTO deactivateTopic(UUID topicId) {
        Topic t = topicRepository.findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Topic not found"));
        t.setActive(false);
        return toTopicDTO(topicRepository.save(t));
    }

    private Subject findOrThrow(UUID id) {
        return subjectRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Subject not found"));
    }

    private SubjectDTO toSubjectDTO(Subject s) {
        return new SubjectDTO(s.getId(), s.getName(), s.getCode(), s.getDescription(),
            s.isDefault(), s.isActive(), s.getTopics().size());
    }

    private TopicDTO toTopicDTO(Topic t) {
        return new TopicDTO(t.getId(), t.getName(), t.getCode(), t.getDescription(),
            t.isDefault(), t.isActive());
    }


    // --- module-boundary lookups (used by other modules' services) ---

    @Transactional(readOnly = true)
    public Subject findSubjectOrThrow(UUID subjectId) {
        return findOrThrow(subjectId);
    }

    @Transactional(readOnly = true)
    public Topic findTopicOrThrow(UUID topicId) {
        return topicRepository.findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Topic not found"));
    }

}
