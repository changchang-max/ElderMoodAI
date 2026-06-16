package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import top.publicnote.eldermoodai.backend.enums.AlertStatus;
import top.publicnote.eldermoodai.backend.enums.AlertType;
import top.publicnote.eldermoodai.backend.enums.Severity;

import java.time.LocalDateTime;

/**
 * 预警记录实体类
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.6, 6.12, 6.13
 */
@Entity
@Table(name = "alert_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的老人ID，必填
     */
    @NotNull(message = "老人ID不能为空")
    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    /**
     * 预警类型：NEGATIVE_EMOTION（负面情绪）、ABNORMAL_BEHAVIOR（异常行为）
     */
    @NotNull(message = "预警类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    /**
     * 严重程度：LOW、MEDIUM、HIGH、CRITICAL
     */
    @NotNull(message = "严重程度不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    /**
     * 预警消息内容，必填，最长500字符
     */
    @NotNull(message = "预警消息不能为空")
    @Size(min = 1, max = 500, message = "预警消息长度必须在1-500字符之间")
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /**
     * 处理状态：PENDING（待处理）、HANDLED（已处理）、IGNORED（已忽略）
     */
    @NotNull(message = "处理状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlertStatus status;

    /**
     * 处理人ID，可选（关联user表）
     */
    @Column(name = "handled_by")
    private Long handledBy;

    /**
     * 处理时间，可选
     */
    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    /**
     * 处理备注，可选，最长1000字符
     */
    @Size(max = 1000, message = "处理备注不能超过1000字符")
    @Column(name = "handle_note", length = 1000)
    private String handleNote;

    /**
     * 创建时间，不可更新
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AlertStatus.PENDING;
        }
    }
}
