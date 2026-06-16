package top.publicnote.eldermoodai.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import top.publicnote.eldermoodai.backend.service.EmailService;

/**
 * 邮件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("ElderMoodAI - 注册验证码");
            message.setText(String.format(
                    "您好！\n\n" +
                    "您正在注册 ElderMoodAI 居家老人情感分析及可视化系统。\n\n" +
                    "您的验证码是: %s\n\n" +
                    "该验证码将在 5 分钟内有效，请尽快使用。\n\n" +
                    "如果这不是您本人的操作，请忽略此邮件。\n\n" +
                    "祝您使用愉快！\n" +
                    "ElderMoodAI 团队",
                    code
            ));

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", toEmail);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码邮件失败，请稍后重试");
        }
    }
}
