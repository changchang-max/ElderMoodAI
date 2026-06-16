package top.publicnote.eldermoodai.backend.exception;

/**
 * Token无效异常
 * 当JWT Token无效或过期时抛出
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
