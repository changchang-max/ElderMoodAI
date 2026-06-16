package top.publicnote.eldermoodai.backend.service;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    /**
     * 生成并存储验证码
     *
     * @param email 邮箱
     * @return 生成的验证码
     */
    String generateAndStoreCode(String email);

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 验证是否成功
     */
    boolean verifyCode(String email, String code);

    /**
     * 删除验证码
     *
     * @param email 邮箱
     */
    void deleteCode(String email);
}
