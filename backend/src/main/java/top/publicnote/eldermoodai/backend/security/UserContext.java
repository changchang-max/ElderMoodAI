package top.publicnote.eldermoodai.backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.publicnote.eldermoodai.backend.enums.UserRole;

/**
 * 用户上下文
 * 存储当前请求的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户角色
     */
    private UserRole role;
}
