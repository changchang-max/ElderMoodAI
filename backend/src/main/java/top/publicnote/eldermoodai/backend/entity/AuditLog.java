package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * 审计日志实体类（只读，仅支持写入，不允许修改）
 * Requirements: 9.1, 9.9, 9.10, 9.11
 */
@Entity
@Table(name = "audit_log")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID，必填
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 操作动作，必填，如 LOGIN、LOGOUT、CREATE_ELDER 等
     */
    @NotNull(message = "操作动作不能为空")
    @Size(min = 1, max = 100, message = "操作动作长度必须在1-100字符之间")
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * 资源类型，必填，如 USER、ELDER、EMOTION_RECORD 等
     */
    @NotNull(message = "资源类型不能为空")
    @Size(min = 1, max = 50, message = "资源类型长度必须在1-50字符之间")
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * 资源ID，可选
     */
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * 客户端IP地址，可选，最长50字符
     */
    @Size(max = 50, message = "IP地址不能超过50字符")
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 详细信息（JSON格式），可选，最长1000字符
     */
    @Size(max = 1000, message = "详细信息不能超过1000字符")
    @Column(name = "details", length = 1000)
    private String details;

    /**
     * 创建时间，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
