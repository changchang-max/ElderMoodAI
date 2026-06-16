package top.publicnote.eldermoodai.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.publicnote.eldermoodai.backend.dto.ApiResponse;
import top.publicnote.eldermoodai.backend.dto.RegisterRequest;
import top.publicnote.eldermoodai.backend.dto.SendVerificationCodeRequest;
import top.publicnote.eldermoodai.backend.entity.User;
import top.publicnote.eldermoodai.backend.service.UserService;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证和注册相关接口")
public class AuthController {

    private final UserService userService;

    @PostMapping("/send-verification-code")
    @Operation(summary = "发送注册验证码", description = "向用户邮箱发送注册验证码")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        try {
            userService.sendRegistrationCode(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("验证码已发送，请查收邮箱"));
        } catch (IllegalArgumentException e) {
            log.warn("发送验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("发送验证码失败，请稍后重试"));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用邮箱和验证码进行用户注册")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("注册成功", user));
        } catch (IllegalArgumentException e) {
            log.warn("注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("注册异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册失败，请稍后重试"));
        }
    }
}
