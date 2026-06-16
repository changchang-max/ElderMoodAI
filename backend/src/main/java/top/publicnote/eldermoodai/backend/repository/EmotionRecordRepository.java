package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.EmotionRecord;
import top.publicnote.eldermoodai.backend.enums.EmotionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 情感记录Repository接口
 * Requirements: 5.2, 5.4, 5.5, 5.7
 */
@Repository
public interface EmotionRecordRepository extends JpaRepository<EmotionRecord, Long> {

    /**
     * 根据老人ID和时间范围查询情感记录列表
     */
    List<EmotionRecord> findByElderIdAndAnalyzedAtBetween(Long elderId, LocalDateTime start, LocalDateTime end);

    /**
     * 根据老人ID查询情感记录（按分析时间倒序分页）
     */
    Page<EmotionRecord> findByElderIdOrderByAnalyzedAtDesc(Long elderId, Pageable pageable);

    /**
     * 统计指定老人在时间范围内特定情感类型的记录数
     */
    long countByElderIdAndEmotionTypeAndAnalyzedAtBetween(Long elderId, EmotionType emotionType, LocalDateTime start, LocalDateTime end);
}
