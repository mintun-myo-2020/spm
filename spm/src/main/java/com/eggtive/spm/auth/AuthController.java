package com.eggtive.spm.auth;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.user.dto.UserInfoDTO;
import com.eggtive.spm.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CurrentUserService currentUserService;
    private final UserService userService;

    public AuthController(CurrentUserService currentUserService, UserService userService) {
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoDTO> me() {
        return ApiResponse.ok(userService.getUserInfo(currentUserService.getCurrentUser()));
    }
}
