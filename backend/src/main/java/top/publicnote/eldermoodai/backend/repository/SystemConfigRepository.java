package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.SystemConfig;

import java.util.Optional;

/**
 * 系统配置Repository接口
 * Requirements: 12.1, 12.2, 12.11, 12.12
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键查询系统配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
}
