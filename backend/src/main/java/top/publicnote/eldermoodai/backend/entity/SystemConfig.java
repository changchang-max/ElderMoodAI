package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * Requirements: 12.1, 12.2, 12.11, 12.12
 */
@Entity
@Table(
    name = "system_config",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"config_key"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置键，必填，全局唯一，最长100字符
     */
    @NotNull(message = "配置键不能为空")
    @Size(min = 1, max = 100, message = "配置键长度必须在1-100字符之间")
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置值，必填，最长500字符
     */
    @NotNull(message = "配置值不能为空")
    @Size(min = 1, max = 500, message = "配置值长度必须在1-500字符之间")
    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    /**
     * 配置描述，可选，最长200字符
     */
    @Size(max = 200, message = "描述不能超过200字符")
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 最后更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
