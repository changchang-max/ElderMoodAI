package top.publicnote.eldermoodai.backend.exception;

/**
 * 加密异常
 * 当加密或解密操作失败时抛出
 */
public class EncryptionException extends RuntimeException {
    
    public EncryptionException(String message) {
        super(message);
    }
    
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
