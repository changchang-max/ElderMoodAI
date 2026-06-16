package top.publicnote.eldermoodai.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.publicnote.eldermoodai.backend.entity.User;

import java.util.Optional;

/**
 * 用户Repository接口
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号或邮箱查询用户
     */
    Optional<User> findByPhoneOrEmail(String phone, String email);

    /**
     * 判断手机号是否已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 判断邮箱是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);
}
