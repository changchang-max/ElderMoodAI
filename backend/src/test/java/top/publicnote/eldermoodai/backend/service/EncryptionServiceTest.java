package top.publicnote.eldermoodai.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import top.publicnote.eldermoodai.backend.exception.EncryptionException;

import static org.assertj.core.api.Assertions.*;

/**
 * 加密服务单元测试
 * Requirements: 11.1, 11.9
 */
@SpringBootTest
@ActiveProfiles("test")
class EncryptionServiceTest {
    
    @Autowired
    private EncryptionService encryptionService;
    
    private String testData;
    
    @BeforeEach
    void setUp() {
        testData = "这是一段测试数据，包含中文和English";
    }
    
    /**
     * 测试加密后数据不等于原始数据
     */
    @Test
    void testEncryptedDataNotEqualsPlainText() {
        String encrypted = encryptionService.encrypt(testData);
        assertThat(encrypted).isNotEqualTo(testData);
        assertThat(encrypted).isNotEmpty();
    }
    
    /**
     * 测试解密后数据等于原始数据
     */
    @Test
    void testDecryptedDataEqualsPlainText() {
        String encrypted = encryptionService.encrypt(testData);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(testData);
    }
    
    /**
     * 测试相同数据多次加密结果不同（IV随机性）
     */
    @Test
    void testMultipleEncryptionsProduceDifferentResults() {
        String encrypted1 = encryptionService.encrypt(testData);
        String encrypted2 = encryptionService.encrypt(testData);
        String encrypted3 = encryptionService.encrypt(testData);
        
        // 由于IV是随机生成的，每次加密结果应该不同
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(encrypted2).isNotEqualTo(encrypted3);
        assertThat(encrypted1).isNotEqualTo(encrypted3);
        
        // 但解密后应该都等于原始数据
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(testData);
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(testData);
        assertThat(encryptionService.decrypt(encrypted3)).isEqualTo(testData);
    }
    
    /**
     * 测试空字符串加密抛出异常
     */
    @Test
    void testEncryptEmptyStringThrowsException() {
        assertThatThrownBy(() -> encryptionService.encrypt(""))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    /**
     * 测试null加密抛出异常
     */
    @Test
    void testEncryptNullThrowsException() {
        assertThatThrownBy(() -> encryptionService.encrypt(null))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    /**
     * 测试无效密文解密抛出异常
     */
    @Test
    void testDecryptInvalidDataThrowsException() {
        assertThatThrownBy(() -> encryptionService.decrypt("invalid-encrypted-data"))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("Failed to decrypt data");
    }
    
    /**
     * 测试空字符串解密抛出异常
     */
    @Test
    void testDecryptEmptyStringThrowsException() {
        assertThatThrownBy(() -> encryptionService.decrypt(""))
            .isInstanceOf(EncryptionException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    /**
     * 测试加密和解密长文本
     */
    @Test
    void testEncryptDecryptLongText() {
        String longText = "这是一段很长的文本数据".repeat(100);
        String encrypted = encryptionService.encrypt(longText);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(longText);
    }
    
    /**
     * 测试加密和解密特殊字符
     */
    @Test
    void testEncryptDecryptSpecialCharacters() {
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\n\t\r";
        String encrypted = encryptionService.encrypt(specialChars);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(specialChars);
    }
}
