package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.8
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，必填，唯一，长度3-50字符
     */
    @NotNull(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 手机号，可选，唯一，符合11位数字格式
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Column(unique = true, length = 20)
    private String phone;

    /**
     * 邮箱，可选，唯一，符合邮箱格式
     */
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, length = 100)
    private String email;

    /**
     * 密码哈希，必填，使用BCrypt加密存储
     */
    @NotNull(message = "密码不能为空")
    @Size(max = 255, message = "密码哈希长度不能超过255字符")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * 用户角色：GUARDIAN（家属/监护人）、CAREGIVER（护理员）、ADMIN（管理员）
     */
    @NotNull(message = "用户角色不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * 用户状态：ACTIVE（活跃）、INACTIVE（禁用）、PENDING_APPROVAL（待审批）
     */
    @NotNull(message = "用户状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_APPROVAL;

    /**
     * 创建时间，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = UserStatus.PENDING_APPROVAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
