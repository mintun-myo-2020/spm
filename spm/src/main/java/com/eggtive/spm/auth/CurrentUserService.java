package com.eggtive.spm.auth;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.enums.Role;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.user.entity.*;
import com.eggtive.spm.user.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class CurrentUserService {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

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
            log.warn("getCurrentUser called with no JWT authentication");
            throw new AppException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }
        String keycloakId = jwt.getSubject();
        log.debug("getCurrentUser: keycloakId={}, email={}", keycloakId, jwt.getClaimAsString("email"));
        User user = userRepository.findByKeycloakId(keycloakId)
            .map(existing -> syncRolesFromToken(existing, jwt))
            .orElseGet(() -> {
                log.info("First login — provisioning user from JWT: keycloakId={}, email={}", keycloakId, jwt.getClaimAsString("email"));
                return provisionFromToken(jwt);
            });
        if (!user.isActive()) {
            log.warn("User {} is deactivated", user.getEmail());
            throw new AppException(ErrorCode.FORBIDDEN, "Account is deactivated");
        }
        log.debug("getCurrentUser resolved: id={}, email={}, roles={}", user.getId(), user.getEmail(), user.getRoles());
        return user;
    }

    /** Sync roles from JWT on every login and create missing profile records. */
    @SuppressWarnings("unchecked")
    private User syncRolesFromToken(User user, Jwt jwt) {
        Set<Role> jwtRoles = extractRoles(jwt);
        if (jwtRoles.isEmpty() || jwtRoles.equals(user.getRoles())) {
            return user;
        }
        log.info("Syncing roles for user {}: {} -> {}", user.getEmail(), user.getRoles(), jwtRoles);
        user.setRoles(jwtRoles);
        user = userRepository.save(user);
        ensureProfileExists(user, jwtRoles);
        return user;
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
     * If a user with the same email already exists (e.g. admin-created with pending keycloakId),
     * links the existing record to this Keycloak account instead of creating a duplicate.
     */
    @SuppressWarnings("unchecked")
    private User provisionFromToken(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : keycloakId;
        String firstName = jwt.getClaimAsString("given_name") != null ? jwt.getClaimAsString("given_name") : "";
        String lastName = jwt.getClaimAsString("family_name") != null ? jwt.getClaimAsString("family_name") : "";

        Set<Role> roles = extractRoles(jwt);
        log.info("provisionFromToken: email={}, resolved roles={}", email, roles);

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getKeycloakId() == null || user.getKeycloakId().startsWith("pending-")) {
                user.setKeycloakId(keycloakId);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (!roles.isEmpty()) {
                    user.setRoles(roles);
                }
                user = userRepository.save(user);
                ensureProfileExists(user, roles);
                return user;
            }
            user.setKeycloakId(keycloakId);
            return userRepository.save(user);
        }

        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user = userRepository.save(user);
        ensureProfileExists(user, roles);
        return user;
    }

    @SuppressWarnings("unchecked")
    private Set<Role> extractRoles(Jwt jwt) {
        Set<Role> roles = new HashSet<>();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            for (String name : (List<String>) realmAccess.get("roles")) {
                try { roles.add(Role.valueOf(name.toUpperCase())); }
                catch (IllegalArgumentException ignored) { log.trace("Skipping unmapped Keycloak role: {}", name); }
            }
        }
        return roles;
    }

    private void ensureProfileExists(User user, Set<Role> roles) {
        if (roles.contains(Role.TEACHER) && teacherRepository.findByUserId(user.getId()).isEmpty()) {
            log.info("Creating Teacher profile for user {}", user.getEmail());
            Teacher t = new Teacher();
            t.setUser(user);
            teacherRepository.save(t);
        }
        if (roles.contains(Role.STUDENT) && studentRepository.findByUserId(user.getId()).isEmpty()) {
            log.info("Creating Student profile for user {}", user.getEmail());
            Student s = new Student();
            s.setUser(user);
            s.setEnrollmentDate(LocalDate.now());
            studentRepository.save(s);
        }
        if (roles.contains(Role.PARENT) && parentRepository.findByUserId(user.getId()).isEmpty()) {
            log.info("Creating Parent profile for user {}", user.getEmail());
            Parent p = new Parent();
            p.setUser(user);
            parentRepository.save(p);
        }
    }
}
