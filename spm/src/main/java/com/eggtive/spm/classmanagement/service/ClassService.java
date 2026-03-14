package com.eggtive.spm.classmanagement.service;

import com.eggtive.spm.classmanagement.dto.*;
import com.eggtive.spm.classmanagement.entity.ClassStudent;
import com.eggtive.spm.classmanagement.entity.TuitionClass;
import com.eggtive.spm.classmanagement.repository.ClassStudentRepository;
import com.eggtive.spm.classmanagement.repository.TuitionClassRepository;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.EnrollmentStatus;
import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.subject.entity.Subject;
import com.eggtive.spm.subject.service.SubjectService;
import com.eggtive.spm.user.entity.Student;
import com.eggtive.spm.user.entity.Teacher;
import com.eggtive.spm.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ClassService {

    private final TuitionClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final SubjectService subjectService;
    private final UserService userService;

    public ClassService(TuitionClassRepository classRepo, ClassStudentRepository csRepo,
                        SubjectService subjectService, UserService userService) {
        this.classRepository = classRepo;
        this.classStudentRepository = csRepo;
        this.subjectService = subjectService;
        this.userService = userService;
    }

    public ClassDTO createClass(CreateClassRequestDTO req) {
        Subject subject = subjectService.findSubjectOrThrow(req.subjectId());
        Teacher teacher = userService.findTeacherOrThrow(req.teacherId());

        TuitionClass tc = new TuitionClass();
        tc.setName(req.name());
        tc.setSubject(subject);
        tc.setTeacher(teacher);
        tc.setDescription(req.description());
        if (req.maxStudents() != null) tc.setMaxStudents(req.maxStudents());
        tc = classRepository.save(tc);
        return toClassDTO(tc);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClassDTO> getAllClasses(Pageable pageable) {
        Page<TuitionClass> page = classRepository.findAll(pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toClassDTO).toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClassDTO> getTeacherClasses(UUID teacherId, Pageable pageable) {
        Page<TuitionClass> page = classRepository.findByTeacherIdAndIsActiveTrue(teacherId, pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toClassDTO).toList());
    }

    public EnrollmentDTO enrollStudent(UUID classId, UUID studentId) {
        TuitionClass tc = findClassOrThrow(classId);
        Student student = userService.findStudentOrThrow(studentId);

        long count = classRepository.countActiveStudents(classId);
        if (count >= tc.getMaxStudents()) {
            throw new AppException(ErrorCode.CLASS_FULL, "Class is at maximum capacity");
        }
        if (classStudentRepository.existsByTuitionClassIdAndStudentIdAndStatus(classId, studentId, EnrollmentStatus.ACTIVE)) {
            throw new AppException(ErrorCode.CONFLICT, "Student already enrolled in this class");
        }

        ClassStudent cs = new ClassStudent();
        cs.setTuitionClass(tc);
        cs.setStudent(student);
        cs.setEnrollmentDate(LocalDate.now());
        cs.setStatus(EnrollmentStatus.ACTIVE);
        cs = classStudentRepository.save(cs);
        return toEnrollmentDTO(cs);
    }

    public EnrollmentDTO withdrawStudent(UUID classId, UUID studentId) {
        ClassStudent cs = classStudentRepository.findByTuitionClassIdAndStudentId(classId, studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Enrollment not found"));
        cs.setStatus(EnrollmentStatus.WITHDRAWN);
        cs.setWithdrawalDate(LocalDate.now());
        return toEnrollmentDTO(classStudentRepository.save(cs));
    }
    public EnrollmentDTO reEnrollStudent(UUID classId, UUID studentId) {
        ClassStudent cs = classStudentRepository.findByTuitionClassIdAndStudentId(classId, studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Enrollment not found"));
        if (cs.getStatus() != EnrollmentStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.CONFLICT, "Student is not in withdrawn status");
        }
        TuitionClass tc = findClassOrThrow(classId);
        long count = classRepository.countActiveStudents(classId);
        if (count >= tc.getMaxStudents()) {
            throw new AppException(ErrorCode.CLASS_FULL, "Class is at maximum capacity");
        }
        cs.setStatus(EnrollmentStatus.ACTIVE);
        cs.setWithdrawalDate(null);
        return toEnrollmentDTO(classStudentRepository.save(cs));
    }

    public ClassDTO changeTeacher(UUID classId, UUID newTeacherId) {
        TuitionClass tc = findClassOrThrow(classId);
        Teacher newTeacher = userService.findTeacherOrThrow(newTeacherId);
        tc.setTeacher(newTeacher);
        return toClassDTO(classRepository.save(tc));
    }

    @Transactional(readOnly = true)
    public ClassDetailDTO getClassDetail(UUID classId) {
        TuitionClass tc = findClassOrThrow(classId);
        ClassDTO base = toClassDTO(tc);
        List<ClassStudentDTO> students = classStudentRepository.findByTuitionClassId(classId).stream()
            .map(cs -> {
                Student s = cs.getStudent();
                String name = s.getUser().getFirstName() + " " + s.getUser().getLastName();
                return new ClassStudentDTO(s.getId(), name, s.getUser().getEmail(),
                    cs.getEnrollmentDate(), cs.getStatus().name());
            }).toList();
        return new ClassDetailDTO(base.id(), base.name(), base.subjectId(), base.subjectName(),
            base.teacherId(), base.teacherName(), base.description(), base.maxStudents(),
            base.currentStudentCount(), base.isActive(), base.createdAt(), students);
    }

    @Transactional(readOnly = true)
    public List<ClassDTO> getStudentEnrolledClasses(UUID studentId) {
        userService.findStudentOrThrow(studentId);
        return classStudentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE).stream()
            .map(cs -> toClassDTO(cs.getTuitionClass()))
            .toList();
    }


    public void verifyTeacherOwnsClass(UUID classId, UUID teacherId) {
        TuitionClass tc = findClassOrThrow(classId);
        if (!tc.getTeacher().getId().equals(teacherId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "You can only manage students in your own classes");
        }
    }

    public TuitionClass findClassOrThrow(UUID classId) {
        return classRepository.findById(classId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Class not found"));
    }

    private ClassDTO toClassDTO(TuitionClass tc) {
        long count = classRepository.countActiveStudents(tc.getId());
        Teacher t = tc.getTeacher();
        String teacherName = t.getUser().getFirstName() + " " + t.getUser().getLastName();
        return new ClassDTO(tc.getId(), tc.getName(), tc.getSubject().getId(), tc.getSubject().getName(),
            t.getId(), teacherName, tc.getDescription(), tc.getMaxStudents(), count,
            tc.isActive(), tc.getCreatedAt());
    }

    private EnrollmentDTO toEnrollmentDTO(ClassStudent cs) {
        Student s = cs.getStudent();
        String name = s.getUser().getFirstName() + " " + s.getUser().getLastName();
        return new EnrollmentDTO(cs.getId(), cs.getTuitionClass().getId(), s.getId(),
            name, cs.getEnrollmentDate(), cs.getStatus().name());
    }
}

