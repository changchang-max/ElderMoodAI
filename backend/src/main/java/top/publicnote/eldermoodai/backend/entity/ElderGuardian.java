package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 老人-监护人关系实体类
 * Requirements: 4.1, 4.2, 4.3, 4.5
 */
@Entity
@Table(
        name = "elder_guardian",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"elder_id", "guardian_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElderGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 老人ID，必填，外键关联 elder.id
     */
    @NotNull(message = "老人ID不能为空")
    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    /**
     * 监护人ID，必填，外键关联 user.id
     */
    @NotNull(message = "监护人ID不能为空")
    @Column(name = "guardian_id", nullable = false)
    private Long guardianId;

    /**
     * 关系描述，必填，如：子女、配偶、护理员等，最长50字符
     */
    @NotNull(message = "关系描述不能为空")
    @Size(min = 1, max = 50, message = "关系描述长度必须在1-50字符之间")
    @Column(nullable = false, length = 50)
    private String relationship;

    /**
     * 是否已授权，默认 false
     */
    @NotNull(message = "授权状态不能为空")
    @Column(nullable = false)
    @Builder.Default
    private Boolean authorized = false;

    /**
     * 创建时间，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.authorized == null) {
            this.authorized = false;
        }
    }
}
