package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.ElderGuardian;

import java.util.List;
import java.util.Optional;

/**
 * 老人-监护人关系Repository接口
 * Requirements: 4.1, 4.2, 4.3, 4.5
 */
@Repository
public interface ElderGuardianRepository extends JpaRepository<ElderGuardian, Long> {

    /**
     * 根据老人ID和监护人ID查询关系
     */
    Optional<ElderGuardian> findByElderIdAndGuardianId(Long elderId, Long guardianId);

    /**
     * 根据老人ID和授权状态查询关系列表
     */
    List<ElderGuardian> findByElderIdAndAuthorized(Long elderId, Boolean authorized);

    /**
     * 根据监护人ID和授权状态查询关系列表
     */
    List<ElderGuardian> findByGuardianIdAndAuthorized(Long guardianId, Boolean authorized);

    /**
     * 判断指定老人ID、监护人ID和授权状态的关系是否存在
     */
    boolean existsByElderIdAndGuardianIdAndAuthorized(Long elderId, Long guardianId, Boolean authorized);
}
