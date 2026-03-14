package com.eggtive.spm.user.service;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.user.dto.*;
import com.eggtive.spm.user.entity.*;
import com.eggtive.spm.user.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    public UserService(UserRepository userRepo, TeacherRepository teacherRepo,
                       StudentRepository studentRepo, ParentRepository parentRepo) {
        this.userRepository = userRepo;
        this.teacherRepository = teacherRepo;
        this.studentRepository = studentRepo;
        this.parentRepository = parentRepo;
    }

    public TeacherDTO createTeacher(CreateTeacherRequestDTO req) {
        assertEmailAvailable(req.email());
        User user = createUser(req.email(), req.firstName(), req.lastName(), req.phoneNumber(), Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setSpecialization(req.specialization());
        return toTeacherDTO(teacherRepository.save(teacher));
    }

    public StudentDTO createStudent(CreateStudentRequestDTO req) {
        assertEmailAvailable(req.email());
        User user = createUser(req.email(), req.firstName(), req.lastName(), null, Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setDateOfBirth(req.dateOfBirth());
        student.setGrade(req.grade());
        student.setEnrollmentDate(LocalDate.now());
        return toStudentDTO(studentRepository.save(student));
    }

    public ParentDTO createParent(CreateParentRequestDTO req) {
        assertEmailAvailable(req.email());
        Student student = studentRepository.findById(req.studentId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student not found"));
        User user = createUser(req.email(), req.firstName(), req.lastName(), req.phoneNumber(), Role.PARENT);
        Parent parent = new Parent();
        parent.setUser(user);
        parent = parentRepository.save(parent);
        student.setParent(parent);
        studentRepository.save(student);
        return toParentDTO(parent, student);
    }

    public void deactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(false);
        user.setDeactivatedAt(Instant.now());
        userRepository.save(user);
    }

    public void reactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(true);
        user.setDeactivatedAt(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserInfoDTO getUserInfo(User user) {
        UUID profileId = null;
        String profileType = null;
        if (user.hasRole(Role.TEACHER)) {
            profileId = teacherRepository.findByUserId(user.getId()).map(Teacher::getId).orElse(null);
            profileType = "TEACHER";
        } else if (user.hasRole(Role.PARENT)) {
            profileId = parentRepository.findByUserId(user.getId()).map(Parent::getId).orElse(null);
            profileType = "PARENT";
        } else if (user.hasRole(Role.STUDENT)) {
            profileId = studentRepository.findByUserId(user.getId()).map(Student::getId).orElse(null);
            profileType = "STUDENT";
        } else if (user.hasRole(Role.ADMIN)) {
            profileType = "ADMIN";
        }
        return new UserInfoDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
            user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()), profileId, profileType);
    }

    // --- helpers ---

    private User createUser(String email, String firstName, String lastName, String phone, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phone);
        user.setKeycloakId("pending-" + UUID.randomUUID());
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private void assertEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.CONFLICT, "Email already exists");
        }
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private TeacherDTO toTeacherDTO(Teacher t) {
        User u = t.getUser();
        return new TeacherDTO(t.getId(), u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
            u.getPhoneNumber(), t.getSpecialization(), u.isActive(), t.getCreatedAt());
    }

    private StudentDTO toStudentDTO(Student s) {
        User u = s.getUser();
        Parent p = s.getParent();
        return new StudentDTO(s.getId(), u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
            s.getDateOfBirth(), s.getGrade(), s.getEnrollmentDate(),
            p != null ? p.getId() : null,
            p != null ? p.getUser().getFirstName() + " " + p.getUser().getLastName() : null,
            u.isActive(), s.getCreatedAt());
    }

    private ParentDTO toParentDTO(Parent p, Student s) {
        User u = p.getUser();
        return new ParentDTO(p.getId(), u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
            u.getPhoneNumber(), s.getId(),
            s.getUser().getFirstName() + " " + s.getUser().getLastName(),
            p.getPreferredContactMethod().name(), p.isEmailNotificationsEnabled(),
            p.isSmsNotificationsEnabled(), u.isActive(), p.getCreatedAt());
    }


    // --- module-boundary lookups (used by other modules' services) ---

    @Transactional(readOnly = true)
    public Student findStudentOrThrow(UUID studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Student not found"));
    }

    @Transactional(readOnly = true)
    public Teacher findTeacherOrThrow(UUID teacherId) {
        return teacherRepository.findById(teacherId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Teacher not found"));
    }

    @Transactional(readOnly = true)
    public boolean studentExists(UUID studentId) {
        return studentRepository.existsById(studentId);
    }

}
