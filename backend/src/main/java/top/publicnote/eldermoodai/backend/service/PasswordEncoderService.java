package top.publicnote.eldermoodai.backend.service;

/**
 * 密码加密服务接口
 * 使用BCrypt算法进行密码加密和验证
 * Requirements: 1.9, 2.5
 */
public interface PasswordEncoderService {
    
    /**
     * 加密密码
     * 使用BCrypt算法，cost factor为12
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码哈希
     */
    String encode(String rawPassword);
    
    /**
     * 验证密码
     * 使用常量时间比较算法防止时序攻击
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码哈希
     * @return 密码是否匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
}
