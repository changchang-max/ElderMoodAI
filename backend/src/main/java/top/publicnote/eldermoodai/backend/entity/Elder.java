package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import top.publicnote.eldermoodai.backend.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人实体类
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.7
 */
@Entity
@Table(name = "elder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Elder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 姓名，必填，长度1-50字符
     */
    @NotNull(message = "姓名不能为空")
    @Size(min = 1, max = 50, message = "姓名长度必须在1-50字符之间")
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 性别：MALE（男）、FEMALE（女）、OTHER（其他）
     */
    @NotNull(message = "性别不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    /**
     * 出生日期，必填，必须是过去的日期
     */
    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的日期")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /**
     * 健康状况描述，可选，最长500字符
     */
    @Size(max = 500, message = "健康状况描述不能超过500字符")
    @Column(name = "health_status", length = 500)
    private String healthStatus;

    /**
     * 隐私保护开关，默认开启
     */
    @NotNull(message = "隐私保护开关不能为空")
    @Column(name = "privacy_enabled", nullable = false)
    @Builder.Default
    private Boolean privacyEnabled = true;

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
        if (this.privacyEnabled == null) {
            this.privacyEnabled = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
