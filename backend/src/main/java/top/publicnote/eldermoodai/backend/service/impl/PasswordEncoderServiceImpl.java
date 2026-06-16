package top.publicnote.eldermoodai.backend.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import top.publicnote.eldermoodai.backend.service.PasswordEncoderService;

/**
 * 密码加密服务实现类
 * 使用BCrypt算法进行密码加密和验证
 * Requirements: 1.9, 2.5
 */
@Service
public class PasswordEncoderServiceImpl implements PasswordEncoderService {
    
    /**
     * BCrypt cost factor设置为12
     * 提供足够的安全性同时保持合理的性能
     */
    private static final int BCRYPT_STRENGTH = 12;
    
    private final BCryptPasswordEncoder encoder;
    
    public PasswordEncoderServiceImpl() {
        this.encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码哈希
     */
    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * BCrypt内部使用常量时间比较算法，防止时序攻击
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码哈希
     * @return 密码是否匹配
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}
