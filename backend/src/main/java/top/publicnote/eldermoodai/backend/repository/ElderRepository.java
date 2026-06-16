package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.Elder;

import java.util.Optional;

/**
 * 老人Repository接口
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.7
 */
@Repository
public interface ElderRepository extends JpaRepository<Elder, Long> {

    /**
     * 根据ID和隐私保护状态查询老人
     */
    Optional<Elder> findByIdAndPrivacyEnabled(Long id, Boolean privacyEnabled);
}
