package top.publicnote.eldermoodai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import top.publicnote.eldermoodai.backend.enums.EmotionDataSource;
import top.publicnote.eldermoodai.backend.enums.EmotionType;

import java.time.LocalDateTime;

/**
 * 情感记录实体类
 * Requirements: 5.2, 5.4, 5.5, 5.7
 */
@Entity
@Table(name = "emotion_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionRecord {

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
     * 情感类型：HAPPY, CALM, SAD, ANXIOUS, ANGRY
     */
    @NotNull(message = "情感类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "emotion_type", nullable = false, length = 20)
    private EmotionType emotionType;

    /**
     * 置信度分数，范围 0.0 ~ 1.0，必填
     */
    @NotNull(message = "置信度分数不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "置信度分数不能小于0.0")
    @DecimalMax(value = "1.0", inclusive = true, message = "置信度分数不能大于1.0")
    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    /**
     * 数据来源：VOICE, IMAGE, VIDEO, TEXT, SENSOR
     */
    @NotNull(message = "数据来源不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 20)
    private EmotionDataSource dataSource;

    /**
     * 原始数据存储路径（加密），可选，最长500字符
     */
    @Size(max = 500, message = "原始数据URL不能超过500字符")
    @Column(name = "raw_data_url", length = 500)
    private String rawDataUrl;

    /**
     * 分析时间，必填
     */
    @NotNull(message = "分析时间不能为空")
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

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
