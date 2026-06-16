package top.publicnote.eldermoodai.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * 密码加密服务单元测试
 * Requirements: 1.9
 */
@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderServiceTest {
    
    @Autowired
    private PasswordEncoderService passwordEncoderService;
    
    /**
     * 测试相同密码多次加密结果不同（盐随机性）
     */
    @Test
    void testMultipleEncodingsProduceDifferentResults() {
        String password = "TestPassword123!";
        
        String encoded1 = passwordEncoderService.encode(password);
        String encoded2 = passwordEncoderService.encode(password);
        String encoded3 = passwordEncoderService.encode(password);
        
        // 由于盐是随机生成的，每次加密结果应该不同
        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(encoded2).isNotEqualTo(encoded3);
        assertThat(encoded1).isNotEqualTo(encoded3);
        
        // 但都应该能够验证成功
        assertThat(passwordEncoderService.matches(password, encoded1)).isTrue();
        assertThat(passwordEncoderService.matches(password, encoded2)).isTrue();
        assertThat(passwordEncoderService.matches(password, encoded3)).isTrue();
    }
    
    /**
     * 测试正确密码验证成功
     */
    @Test
    void testMatchesWithCorrectPassword() {
        String password = "CorrectPassword123!";
        String encoded = passwordEncoderService.encode(password);
        
        boolean matches = passwordEncoderService.matches(password, encoded);
        assertThat(matches).isTrue();
    }
    
    /**
     * 测试错误密码验证失败
     */
    @Test
    void testMatchesWithIncorrectPassword() {
        String password = "CorrectPassword123!";
        String wrongPassword = "WrongPassword456!";
        String encoded = passwordEncoderService.encode(password);
        
        boolean matches = passwordEncoderService.matches(wrongPassword, encoded);
        assertThat(matches).isFalse();
    }
    
    /**
     * 测试空密码加密抛出异常
     */
    @Test
    void testEncodeEmptyPasswordThrowsException() {
        assertThatThrownBy(() -> passwordEncoderService.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }
    
    /**
     * 测试null密码加密抛出异常
     */
    @Test
    void testEncodeNullPasswordThrowsException() {
        assertThatThrownBy(() -> passwordEncoderService.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }
    
    /**
     * 测试null密码验证返回false
     */
    @Test
    void testMatchesWithNullPasswordReturnsFalse() {
        String encoded = passwordEncoderService.encode("TestPassword123!");
        
        boolean matches = passwordEncoderService.matches(null, encoded);
        assertThat(matches).isFalse();
    }
    
    /**
     * 测试null编码密码验证返回false
     */
    @Test
    void testMatchesWithNullEncodedPasswordReturnsFalse() {
        boolean matches = passwordEncoderService.matches("TestPassword123!", null);
        assertThat(matches).isFalse();
    }
    
    /**
     * 测试BCrypt编码格式
     * BCrypt编码应该以$2a$或$2b$开头，后跟cost factor
     */
    @Test
    void testBCryptEncodingFormat() {
        String password = "TestPassword123!";
        String encoded = passwordEncoderService.encode(password);
        
        // BCrypt格式：$2a$12$...（$2a$表示BCrypt，12表示cost factor）
        assertThat(encoded).startsWith("$2");
        assertThat(encoded).contains("$12$");
    }
    
    /**
     * 测试长密码加密和验证
     * 注意：BCrypt最大支持72字节的密码
     */
    @Test
    void testEncodeLongPassword() {
        // BCrypt最大支持72字节，使用接近上限的密码进行测试
        String longPassword = "ThisIsALongPassword123!@#$%^&*()_+-=[]{}|;";
        assertThat(longPassword.getBytes().length).isLessThanOrEqualTo(72);
        
        String encoded = passwordEncoderService.encode(longPassword);
        
        boolean matches = passwordEncoderService.matches(longPassword, encoded);
        assertThat(matches).isTrue();
    }
    
    /**
     * 测试特殊字符密码加密和验证
     */
    @Test
    void testEncodePasswordWithSpecialCharacters() {
        String specialPassword = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String encoded = passwordEncoderService.encode(specialPassword);
        
        boolean matches = passwordEncoderService.matches(specialPassword, encoded);
        assertThat(matches).isTrue();
    }
}
