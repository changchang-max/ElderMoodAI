package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.AlertRecord;
import top.publicnote.eldermoodai.backend.enums.AlertStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警记录Repository接口
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.6
 */
@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {

    /**
     * 根据老人ID和处理状态查询预警记录列表
     */
    List<AlertRecord> findByElderIdAndStatus(Long elderId, AlertStatus status);

    /**
     * 根据老人ID和处理状态查询预警记录（按创建时间倒序分页）
     */
    Page<AlertRecord> findByElderIdAndStatusOrderByCreatedAtDesc(Long elderId, AlertStatus status, Pageable pageable);

    /**
     * 统计指定老人在指定时间之后特定状态的预警记录数
     */
    long countByElderIdAndStatusAndCreatedAtAfter(Long elderId, AlertStatus status, LocalDateTime after);
}
