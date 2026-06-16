package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository接口
 * Requirements: 9.1, 9.9, 9.10, 9.11
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据用户ID和时间范围查询审计日志（分页）
     */
    Page<AuditLog> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 根据操作动作和时间查询指定时间之后的审计日志列表
     */
    List<AuditLog> findByActionAndCreatedAtAfter(String action, LocalDateTime after);
}
