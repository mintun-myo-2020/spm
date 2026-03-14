package com.eggtive.spm.auth;

import com.eggtive.spm.common.enums.ErrorCode;
import com.eggtive.spm.common.exception.AppException;
import com.eggtive.spm.user.entity.User;
import com.eggtive.spm.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found for keycloak ID"));
    }

    public String getKeycloakId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Not authenticated");
        }
        return jwt.getSubject();
    }
}
