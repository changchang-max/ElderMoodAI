package top.publicnote.eldermoodai.backend.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String toEmail, String code);
}
