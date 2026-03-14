package com.eggtive.spm.auth;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.user.entity.*;
import com.eggtive.spm.user.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    public CurrentUserService(UserRepository userRepository,
                              TeacherRepository teacherRepository,
                              StudentRepository studentRepository,
                              ParentRepository parentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.parentRepository = parentRepository;
    }

    @Transactional
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
            .orElseGet(() -> provisionFromToken(jwt));
    }

    public String getKeycloakId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }
        return jwt.getSubject();
    }

    /**
     * First-login provisioning: creates a User row (and matching profile row)
     * from the Keycloak JWT so the app DB stays in sync with Keycloak users.
     */
    @SuppressWarnings("unchecked")
    private User provisionFromToken(Jwt jwt) {
        User user = new User();
        user.setKeycloakId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : jwt.getSubject());
        user.setFirstName(jwt.getClaimAsString("given_name") != null ? jwt.getClaimAsString("given_name") : "");
        user.setLastName(jwt.getClaimAsString("family_name") != null ? jwt.getClaimAsString("family_name") : "");

        Set<Role> roles = new HashSet<>();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            for (String name : (List<String>) realmAccess.get("roles")) {
                try { roles.add(Role.valueOf(name.toUpperCase())); }
                catch (IllegalArgumentException ignored) { /* skip unmapped roles */ }
            }
        }
        user.setRoles(roles);
        user = userRepository.save(user);

        // Create the matching profile record so profileType/profileId resolve immediately
        if (roles.contains(Role.TEACHER)) {
            Teacher t = new Teacher();
            t.setUser(user);
            teacherRepository.save(t);
        } else if (roles.contains(Role.STUDENT)) {
            Student s = new Student();
            s.setUser(user);
            s.setEnrollmentDate(LocalDate.now());
            studentRepository.save(s);
        } else if (roles.contains(Role.PARENT)) {
            Parent p = new Parent();
            p.setUser(user);
            parentRepository.save(p);
        }

        return user;
    }
}
