package top.publicnote.eldermoodai.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.publicnote.eldermoodai.backend.service.VerificationCodeService;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRATION_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generateAndStoreCode(String email) {
        String code = generateCode();
        String key = CODE_PREFIX + email;
        
        // 存储验证码到Redis，5分钟过期
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info("为邮箱 {} 生成验证码", email);
        
        return code;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("验证码不存在或已过期: {}", email);
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            log.info("验证码验证成功: {}", email);
        } else {
            log.warn("验证码验证失败: {}", email);
        }
        
        return isValid;
    }

    @Override
    public void deleteCode(String email) {
        String key = CODE_PREFIX + email;
        redisTemplate.delete(key);
        log.info("删除验证码: {}", email);
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        int code = RANDOM.nextInt(900000) + 100000; // 生成100000-999999之间的随机数
        return String.valueOf(code);
    }
}
