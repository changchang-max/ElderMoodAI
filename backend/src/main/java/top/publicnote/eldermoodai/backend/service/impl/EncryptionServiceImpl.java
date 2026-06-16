package top.publicnote.eldermoodai.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.publicnote.eldermoodai.backend.exception.EncryptionException;
import top.publicnote.eldermoodai.backend.service.EncryptionService;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 数据加密服务实现类
 * 使用AES-256-GCM算法进行数据加密和解密
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */
@Service
public class EncryptionServiceImpl implements EncryptionService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private SecretKey secretKey;
    
    /**
     * 初始化密钥
     * 从配置中读取Base64编码的密钥并解码
     */
    @PostConstruct
    public void init() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
            if (decodedKey.length != 32) {
                throw new IllegalArgumentException("Encryption key must be 256 bits (32 bytes)");
            }
            this.secretKey = new SecretKeySpec(decodedKey, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize encryption key", e);
        }
    }
    
    /**
     * 加密数据
     * 
     * @param plainText 明文数据
     * @return Base64编码的加密数据（IV + 密文）
     * @throws EncryptionException 加密失败时抛出
     */
    @Override
    public String encrypt(String plainText) throws EncryptionException {
        if (plainText == null || plainText.isEmpty()) {
            throw new EncryptionException("Plain text cannot be null or empty");
        }
        
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // 加密数据
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 将IV和加密数据拼接
            byte[] encryptedDataWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedDataWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedDataWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            // 返回Base64编码的结果
            return Base64.getEncoder().encodeToString(encryptedDataWithIv);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }
    
    /**
     * 解密数据
     * 
     * @param encryptedText Base64编码的加密数据
     * @return 解密后的明文数据
     * @throws EncryptionException 解密失败时抛出
     */
    @Override
    public String decrypt(String encryptedText) throws EncryptionException {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new EncryptionException("Encrypted text cannot be null or empty");
        }
        
        try {
            // 解码Base64
            byte[] encryptedDataWithIv = Base64.getDecoder().decode(encryptedText);
            
            if (encryptedDataWithIv.length < GCM_IV_LENGTH) {
                throw new EncryptionException("Invalid encrypted data: too short");
            }
            
            // 提取IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedDataWithIv, 0, iv, 0, GCM_IV_LENGTH);
            
            // 提取加密数据
            byte[] encryptedData = new byte[encryptedDataWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedDataWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }
}
