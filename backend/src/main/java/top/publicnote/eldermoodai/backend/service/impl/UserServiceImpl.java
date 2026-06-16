package top.publicnote.eldermoodai.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.publicnote.eldermoodai.backend.dto.RegisterRequest;
import top.publicnote.eldermoodai.backend.entity.User;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.enums.UserStatus;
import top.publicnote.eldermoodai.backend.repository.UserRepository;
import top.publicnote.eldermoodai.backend.service.EmailService;
import top.publicnote.eldermoodai.backend.service.PasswordEncoderService;
import top.publicnote.eldermoodai.backend.service.UserService;
import top.publicnote.eldermoodai.backend.service.VerificationCodeService;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoderService passwordEncoderService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoderService.encode(request.getPassword()))
                .role(UserRole.GUARDIAN) // 默认注册为家属角色
                .status(UserStatus.ACTIVE) // 邮箱验证通过后直接激活
                .build();

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());

        // 删除已使用的验证码
        verificationCodeService.deleteCode(request.getEmail());

        return savedUser;
    }

    @Override
    public void sendRegistrationCode(String email) {
        // 检查邮箱是否已被注册
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        // 生成验证码
        String code = verificationCodeService.generateAndStoreCode(email);

        // 发送邮件
        emailService.sendVerificationCode(email, code);
        log.info("注册验证码已发送至: {}", email);
    }
}
