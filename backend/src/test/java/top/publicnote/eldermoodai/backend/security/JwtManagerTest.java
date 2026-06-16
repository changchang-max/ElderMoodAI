package top.publicnote.eldermoodai.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.exception.InvalidTokenException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT Manager单元测试
 * Requirements: 1.3, 1.4, 10.10
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtManagerTest {
    
    @Autowired
    private JwtManager jwtManager;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;
    
    /**
     * 测试生成的Token包含正确的userId和role
     */
    @Test
    void testGenerateTokenContainsCorrectUserIdAndRole() {
        Long userId = 1L;
        UserRole role = UserRole.GUARDIAN;
        
        String token = jwtManager.generateToken(userId, role);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        UserContext context = jwtManager.validateToken(token);
        assertThat(context.getUserId()).isEqualTo(userId);
        assertThat(context.getRole()).isEqualTo(role);
    }
    
    /**
     * 测试有效Token验证成功
     */
    @Test
    void testValidateValidToken() {
        Long userId = 123L;
        UserRole role = UserRole.ADMIN;
        
        String token = jwtManager.generateToken(userId, role);
        UserContext context = jwtManager.validateToken(token);
        
        assertThat(context).isNotNull();
        assertThat(context.getUserId()).isEqualTo(userId);
        assertThat(context.getRole()).isEqualTo(role);
    }
    
    /**
     * 测试篡改Token验证失败
     */
    @Test
    void testValidateTamperedTokenThrowsException() {
        String token = jwtManager.generateToken(1L, UserRole.GUARDIAN);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        
        assertThatThrownBy(() -> jwtManager.validateToken(tamperedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");
    }
    
    /**
     * 测试无效格式Token验证失败
     */
    @Test
    void testValidateInvalidFormatTokenThrowsException() {
        assertThatThrownBy(() -> jwtManager.validateToken("invalid.token.format"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");
    }
    
    /**
     * 测试空Token验证失败
     */
    @Test
    void testValidateEmptyTokenThrowsException() {
        assertThatThrownBy(() -> jwtManager.validateToken(""))
                .isInstanceOf(InvalidTokenException.class);
    }
    
    /**
     * 测试不同角色生成不同Token
     */
    @Test
    void testGenerateTokenForDifferentRoles() {
        Long userId = 1L;
        
        String guardianToken = jwtManager.generateToken(userId, UserRole.GUARDIAN);
        String caregiverToken = jwtManager.generateToken(userId, UserRole.CAREGIVER);
        String adminToken = jwtManager.generateToken(userId, UserRole.ADMIN);
        
        assertThat(guardianToken).isNotEqualTo(caregiverToken);
        assertThat(caregiverToken).isNotEqualTo(adminToken);
        
        UserContext guardianContext = jwtManager.validateToken(guardianToken);
        UserContext caregiverContext = jwtManager.validateToken(caregiverToken);
        UserContext adminContext = jwtManager.validateToken(adminToken);
        
        assertThat(guardianContext.getRole()).isEqualTo(UserRole.GUARDIAN);
        assertThat(caregiverContext.getRole()).isEqualTo(UserRole.CAREGIVER);
        assertThat(adminContext.getRole()).isEqualTo(UserRole.ADMIN);
    }
    
    /**
     * 测试extractUserId方法
     */
    @Test
    void testExtractUserId() {
        Long userId = 999L;
        String token = jwtManager.generateToken(userId, UserRole.GUARDIAN);
        
        Long extractedUserId = jwtManager.extractUserId(token);
        assertThat(extractedUserId).isEqualTo(userId);
    }
    
    /**
     * 测试从无效Token提取userId返回null
     */
    @Test
    void testExtractUserIdFromInvalidTokenReturnsNull() {
        Long extractedUserId = jwtManager.extractUserId("invalid.token");
        assertThat(extractedUserId).isNull();
    }

    /**
     * 测试过期Token验证失败并抛出InvalidTokenException
     * Requirements: 1.4
     */
    @Test
    void testValidateExpiredTokenThrowsException() throws InterruptedException {
        // Build an already-expired token using the same key
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        // Set expiration 1 second in the past
        Date expiredDate = new Date(now.getTime() - 1000);

        String expiredToken = Jwts.builder()
                .setSubject("1")
                .claim("role", UserRole.GUARDIAN.name())
                .setIssuedAt(new Date(now.getTime() - 2000))
                .setExpiration(expiredDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtManager.validateToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Token expired");
    }

    /**
     * 测试null Token验证失败并抛出InvalidTokenException
     * Requirements: 1.4
     */
    @Test
    void testValidateNullTokenThrowsException() {
        assertThatThrownBy(() -> jwtManager.validateToken(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    /**
     * 测试Token包含正确的过期时间（配置的过期时间）
     * Requirements: 1.8
     */
    @Test
    void testTokenExpirationTimeIsCorrect() {
        Long userId = 42L;
        UserRole role = UserRole.CAREGIVER;

        long beforeGeneration = System.currentTimeMillis();
        String token = jwtManager.generateToken(userId, role);
        long afterGeneration = System.currentTimeMillis();

        // Parse the token to check expiration
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        io.jsonwebtoken.Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long issuedAt = claims.getIssuedAt().getTime();
        long expiresAt = claims.getExpiration().getTime();
        long actualExpiration = expiresAt - issuedAt;

        // The expiration should match the configured value (within a small tolerance)
        assertThat(actualExpiration).isBetween(expirationTime - 1000, expirationTime + 1000);
    }

    /**
     * 测试生成Token不为空
     * Requirements: 1.1
     */
    @Test
    void testGenerateTokenIsNotNull() {
        String token = jwtManager.generateToken(1L, UserRole.GUARDIAN);
        assertThat(token).isNotNull().isNotEmpty();
    }

    /**
     * 测试验证有效Token返回正确的UserContext（userId, role）
     * Requirements: 1.3
     */
    @Test
    void testValidateValidTokenReturnsCorrectUserContext() {
        Long userId = 55L;
        UserRole role = UserRole.CAREGIVER;

        String token = jwtManager.generateToken(userId, role);
        UserContext context = jwtManager.validateToken(token);

        assertThat(context).isNotNull();
        assertThat(context.getUserId()).isEqualTo(userId);
        assertThat(context.getRole()).isEqualTo(role);
    }
}
