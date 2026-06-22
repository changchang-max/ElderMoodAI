package top.publicnote.eldermoodai.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import top.publicnote.eldermoodai.backend.entity.*;
import top.publicnote.eldermoodai.backend.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository集成测试
 * 使用H2内存数据库进行测试
 * Requirements: 14.7
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElderRepository elderRepository;

    @Autowired
    private ElderGuardianRepository elderGuardianRepository;

    @Autowired
    private EmotionRecordRepository emotionRecordRepository;

    @Autowired
    private AlertRecordRepository alertRecordRepository;

    private User testUser;
    private Elder testElder;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = User.builder()
                .username("testuser")
                .phone("13812345678")
                .email("test@example.com")
                .passwordHash("hashedpassword123")
                .role(UserRole.GUARDIAN)
                .status(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.save(testUser);

        // 创建测试老人
        testElder = Elder.builder()
                .name("张三")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1950, 1, 1))
                .healthStatus("健康")
                .privacyEnabled(true)
                .build();
        testElder = elderRepository.save(testElder);
    }

    @Nested
    @DisplayName("UserRepository测试")
    class UserRepositoryTest {

        @Test
        @DisplayName("根据手机号查询用户")
        void findByPhone() {
            Optional<User> found = userRepository.findByPhone("13812345678");
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据邮箱查询用户")
        void findByEmail() {
            Optional<User> found = userRepository.findByEmail("test@example.com");
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据手机号或邮箱查询用户 - 使用手机号")
        void findByPhoneOrEmailWithPhone() {
            Optional<User> found = userRepository.findByPhoneOrEmail("13812345678", "nonexistent@example.com");
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据手机号或邮箱查询用户 - 使用邮箱")
        void findByPhoneOrEmailWithEmail() {
            Optional<User> found = userRepository.findByPhoneOrEmail("99999999999", "test@example.com");
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("判断手机号是否已存在")
        void existsByPhone() {
            assertThat(userRepository.existsByPhone("13812345678")).isTrue();
            assertThat(userRepository.existsByPhone("19999999999")).isFalse();
        }

        @Test
        @DisplayName("判断邮箱是否已存在")
        void existsByEmail() {
            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }

        @Test
        @DisplayName("根据用户名查询用户")
        void findByUsername() {
            Optional<User> found = userRepository.findByUsername("testuser");
            
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("ElderGuardianRepository测试")
    class ElderGuardianRepositoryTest {

        @Test
        @DisplayName("根据老人ID和监护人ID查询关系")
        void findByElderIdAndGuardianId() {
            // 创建关系
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("子女")
                    .authorized(true)
                    .build();
            elderGuardianRepository.save(eg);

            Optional<ElderGuardian> found = elderGuardianRepository
                    .findByElderIdAndGuardianId(testElder.getId(), testUser.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getRelationship()).isEqualTo("子女");
        }

        @Test
        @DisplayName("唯一约束测试 - 同一老人和监护人不能重复绑定")
        void uniqueConstraintTest() {
            // 创建第一个关系
            ElderGuardian eg1 = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("子女")
                    .authorized(true)
                    .build();
            elderGuardianRepository.save(eg1);

            // 尝试创建重复关系
            ElderGuardian eg2 = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("护理员")
                    .authorized(false)
                    .build();

            // 由于唯一约束，应该抛出异常
            assertThatThrownBy(() -> elderGuardianRepository.saveAndFlush(eg2))
                    .hasRootCauseInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }

        @Test
        @DisplayName("根据老人ID和授权状态查询关系列表")
        void findByElderIdAndAuthorized() {
            // 创建已授权关系
            ElderGuardian eg1 = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("子女")
                    .authorized(true)
                    .build();
            elderGuardianRepository.save(eg1);

            // 创建未授权关系
            User anotherUser = User.builder()
                    .username("anotheruser")
                    .email("another@example.com")
                    .passwordHash("hashedpassword")
                    .role(UserRole.GUARDIAN)
                    .status(UserStatus.ACTIVE)
                    .build();
            anotherUser = userRepository.save(anotherUser);

            ElderGuardian eg2 = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(anotherUser.getId())
                    .relationship("护理员")
                    .authorized(false)
                    .build();
            elderGuardianRepository.save(eg2);

            List<ElderGuardian> authorizedList = elderGuardianRepository
                    .findByElderIdAndAuthorized(testElder.getId(), true);
            List<ElderGuardian> unauthorizedList = elderGuardianRepository
                    .findByElderIdAndAuthorized(testElder.getId(), false);

            assertThat(authorizedList).hasSize(1);
            assertThat(authorizedList.get(0).getRelationship()).isEqualTo("子女");

            assertThat(unauthorizedList).hasSize(1);
            assertThat(unauthorizedList.get(0).getRelationship()).isEqualTo("护理员");
        }

        @Test
        @DisplayName("根据监护人ID和授权状态查询关系列表")
        void findByGuardianIdAndAuthorized() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("子女")
                    .authorized(true)
                    .build();
            elderGuardianRepository.save(eg);

            List<ElderGuardian> list = elderGuardianRepository
                    .findByGuardianIdAndAuthorized(testUser.getId(), true);

            assertThat(list).hasSize(1);
            assertThat(list.get(0).getElderId()).isEqualTo(testElder.getId());
        }

        @Test
        @DisplayName("判断指定老人、监护人和授权状态的关系是否存在")
        void existsByElderIdAndGuardianIdAndAuthorized() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(testElder.getId())
                    .guardianId(testUser.getId())
                    .relationship("子女")
                    .authorized(true)
                    .build();
            elderGuardianRepository.save(eg);

            assertThat(elderGuardianRepository
                    .existsByElderIdAndGuardianIdAndAuthorized(testElder.getId(), testUser.getId(), true))
                    .isTrue();
            assertThat(elderGuardianRepository
                    .existsByElderIdAndGuardianIdAndAuthorized(testElder.getId(), testUser.getId(), false))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("EmotionRecordRepository测试")
    class EmotionRecordRepositoryTest {

        @Test
        @DisplayName("根据老人ID和时间范围查询情感记录")
        void findByElderIdAndAnalyzedAtBetween() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusDays(7);
            LocalDateTime end = now.plusDays(1);

            // 创建情感记录
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(testElder.getId())
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(now)
                    .build();
            emotionRecordRepository.save(record);

            List<EmotionRecord> records = emotionRecordRepository
                    .findByElderIdAndAnalyzedAtBetween(testElder.getId(), start, end);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).getEmotionType()).isEqualTo(EmotionType.HAPPY);
        }

        @Test
        @DisplayName("根据老人ID查询情感记录（按分析时间倒序分页）")
        void findByElderIdOrderByAnalyzedAtDesc() {
            LocalDateTime now = LocalDateTime.now();

            // 创建多条情感记录
            for (int i = 0; i < 25; i++) {
                EmotionRecord record = EmotionRecord.builder()
                        .elderId(testElder.getId())
                        .emotionType(EmotionType.values()[i % EmotionType.values().length])
                        .confidenceScore(0.8 + (i * 0.01))
                        .dataSource(EmotionDataSource.TEXT)
                        .analyzedAt(now.minusHours(i))
                        .build();
                emotionRecordRepository.save(record);
            }

            Page<EmotionRecord> page1 = emotionRecordRepository
                    .findByElderIdOrderByAnalyzedAtDesc(testElder.getId(), PageRequest.of(0, 10));
            Page<EmotionRecord> page2 = emotionRecordRepository
                    .findByElderIdOrderByAnalyzedAtDesc(testElder.getId(), PageRequest.of(1, 10));

            assertThat(page1.getTotalElements()).isEqualTo(25);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(10);

            // 验证按时间倒序排列
            assertThat(page1.getContent().get(0).getAnalyzedAt())
                    .isAfterOrEqualTo(page1.getContent().get(1).getAnalyzedAt());
        }

        @Test
        @DisplayName("统计指定老人在时间范围内特定情感类型的记录数")
        void countByElderIdAndEmotionTypeAndAnalyzedAtBetween() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusDays(7);
            LocalDateTime end = now.plusDays(1);

            // 创建多条情感记录
            for (int i = 0; i < 5; i++) {
                EmotionRecord record = EmotionRecord.builder()
                        .elderId(testElder.getId())
                        .emotionType(EmotionType.HAPPY)
                        .confidenceScore(0.9)
                        .dataSource(EmotionDataSource.VOICE)
                        .analyzedAt(now.minusDays(i))
                        .build();
                emotionRecordRepository.save(record);
            }

            for (int i = 0; i < 3; i++) {
                EmotionRecord record = EmotionRecord.builder()
                        .elderId(testElder.getId())
                        .emotionType(EmotionType.SAD)
                        .confidenceScore(0.85)
                        .dataSource(EmotionDataSource.TEXT)
                        .analyzedAt(now.minusDays(i))
                        .build();
                emotionRecordRepository.save(record);
            }

            long happyCount = emotionRecordRepository
                    .countByElderIdAndEmotionTypeAndAnalyzedAtBetween(testElder.getId(), EmotionType.HAPPY, start, end);
            long sadCount = emotionRecordRepository
                    .countByElderIdAndEmotionTypeAndAnalyzedAtBetween(testElder.getId(), EmotionType.SAD, start, end);

            assertThat(happyCount).isEqualTo(5);
            assertThat(sadCount).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("AlertRecordRepository测试")
    class AlertRecordRepositoryTest {

        @Test
        @DisplayName("根据老人ID和处理状态查询预警记录")
        void findByElderIdAndStatus() {
            // 创建待处理预警
            AlertRecord pending = AlertRecord.builder()
                    .elderId(testElder.getId())
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .build();
            alertRecordRepository.save(pending);

            // 创建已处理预警
            AlertRecord handled = AlertRecord.builder()
                    .elderId(testElder.getId())
                    .alertType(AlertType.ABNORMAL_BEHAVIOR)
                    .severity(Severity.MEDIUM)
                    .message("检测到异常行为")
                    .status(AlertStatus.HANDLED)
                    .handledBy(testUser.getId())
                    .handledAt(LocalDateTime.now())
                    .handleNote("已处理")
                    .build();
            alertRecordRepository.save(handled);

            List<AlertRecord> pendingList = alertRecordRepository
                    .findByElderIdAndStatus(testElder.getId(), AlertStatus.PENDING);
            List<AlertRecord> handledList = alertRecordRepository
                    .findByElderIdAndStatus(testElder.getId(), AlertStatus.HANDLED);

            assertThat(pendingList).hasSize(1);
            assertThat(pendingList.get(0).getAlertType()).isEqualTo(AlertType.NEGATIVE_EMOTION);

            assertThat(handledList).hasSize(1);
            assertThat(handledList.get(0).getHandleNote()).isEqualTo("已处理");
        }

        @Test
        @DisplayName("根据老人ID和处理状态查询预警记录（按创建时间倒序分页）")
        void findByElderIdAndStatusOrderByCreatedAtDesc() {
            LocalDateTime now = LocalDateTime.now();

            // 创建多条预警记录
            for (int i = 0; i < 15; i++) {
                AlertRecord record = AlertRecord.builder()
                        .elderId(testElder.getId())
                        .alertType(AlertType.NEGATIVE_EMOTION)
                        .severity(Severity.values()[i % Severity.values().length])
                        .message("预警消息" + i)
                        .status(AlertStatus.PENDING)
                        .build();
                alertRecordRepository.save(record);
            }

            Page<AlertRecord> page = alertRecordRepository
                    .findByElderIdAndStatusOrderByCreatedAtDesc(testElder.getId(), AlertStatus.PENDING, 
                            PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(15);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.getContent()).hasSize(10);
        }

        @Test
        @DisplayName("统计指定老人在指定时间之后特定状态的预警记录数")
        void countByElderIdAndStatusAndCreatedAtAfter() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);

            // 创建预警记录
            AlertRecord record1 = AlertRecord.builder()
                    .elderId(testElder.getId())
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("预警1")
                    .status(AlertStatus.PENDING)
                    .build();
            alertRecordRepository.save(record1);

            AlertRecord record2 = AlertRecord.builder()
                    .elderId(testElder.getId())
                    .alertType(AlertType.ABNORMAL_BEHAVIOR)
                    .severity(Severity.MEDIUM)
                    .message("预警2")
                    .status(AlertStatus.PENDING)
                    .build();
            alertRecordRepository.save(record2);

            long count = alertRecordRepository
                    .countByElderIdAndStatusAndCreatedAtAfter(testElder.getId(), AlertStatus.PENDING, oneDayAgo);

            assertThat(count).isEqualTo(2);
        }
    }
}
