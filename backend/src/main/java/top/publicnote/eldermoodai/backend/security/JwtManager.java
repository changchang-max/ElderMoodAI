package top.publicnote.eldermoodai.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.exception.InvalidTokenException;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token管理器
 * 负责生成和验证JWT Token
 * Requirements: 1.1, 1.3, 1.8, 1.10, 10.10
 */
@Component
public class JwtManager {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private Long expirationTime;
    
    private SecretKey key;
    
    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成JWT Token
     * 
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证JWT Token并提取用户上下文
     * 
     * @param token JWT Token字符串
     * @return 用户上下文
     * @throws InvalidTokenException Token无效或过期时抛出
     */
    public UserContext validateToken(String token) throws InvalidTokenException {
        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException("Invalid token: token is null or empty");
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Long userId = Long.parseLong(claims.getSubject());
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            return new UserContext(userId, role);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid token", e);
        }
    }
    
    /**
     * 从Token中提取用户ID（不进行完整验证）
     * 
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
