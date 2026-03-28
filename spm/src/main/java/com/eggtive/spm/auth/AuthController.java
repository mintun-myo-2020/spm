package com.eggtive.spm.auth;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.user.dto.UserInfoDTO;
import com.eggtive.spm.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final KeycloakAdminService keycloakAdminService;

    public AuthController(CurrentUserService currentUserService, UserService userService, KeycloakAdminService keycloakAdminService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
        this.keycloakAdminService = keycloakAdminService;
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoDTO> me() {
        return ApiResponse.ok(userService.getUserInfo(currentUserService.getCurrentUser()));
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8) {
            throw new com.eggtive.spm.common.exception.AppException(
                com.eggtive.spm.common.enums.ErrorCode.INVALID_INPUT, "New password must be at least 8 characters");
        }
        var user = currentUserService.getCurrentUser();
        // Verify current password by attempting a token exchange with Keycloak
        keycloakAdminService.verifyPassword(user.getEmail(), currentPassword);
        // Set new password (non-temporary since user is changing their own)
        keycloakAdminService.resetPasswordNonTemporary(user.getKeycloakId(), newPassword);
        return ApiResponse.ok(null, "Password changed");
    }
}
