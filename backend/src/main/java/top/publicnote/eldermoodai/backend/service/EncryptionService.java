package top.publicnote.eldermoodai.backend.service;

import top.publicnote.eldermoodai.backend.exception.EncryptionException;

/**
 * 数据加密服务接口
 * 使用AES-256-GCM算法进行数据加密和解密
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */
public interface EncryptionService {
    
    /**
     * 加密数据
     * 使用AES-256-GCM算法加密，生成随机12字节IV
     * 
     * @param plainText 明文数据
     * @return Base64编码的加密数据（IV + 密文）
     * @throws EncryptionException 加密失败时抛出
     */
    String encrypt(String plainText) throws EncryptionException;
    
    /**
     * 解密数据
     * 从加密数据中提取IV并解密
     * 
     * @param encryptedText Base64编码的加密数据
     * @return 解密后的明文数据
     * @throws EncryptionException 解密失败时抛出
     */
    String decrypt(String encryptedText) throws EncryptionException;
}
