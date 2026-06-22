package top.publicnote.eldermoodai.backend.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import top.publicnote.eldermoodai.backend.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * 实体类验证测试
 * Requirements: 2.1, 3.1, 4.5, 5.5, 6.12
 */
class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("User实体验证测试")
    class UserValidationTest {

        @Test
        @DisplayName("有效的User实体应通过验证")
        void validUserShouldPassValidation() {
            User user = User.builder()
                    .username("testuser")
                    .phone("13812345678")
                    .email("test@example.com")
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .status(UserStatus.ACTIVE)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("用户名不能为空")
        void usernameCannotBeNull() {
            User user = User.builder()
                    .username(null)
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("用户名不能为空"));
        }

        @Test
        @DisplayName("用户名长度必须在3-50字符之间")
        void usernameSizeMustBeBetween3And50() {
            // 用户名太短
            User shortUsername = User.builder()
                    .username("ab")
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> shortViolations = validator.validate(shortUsername);
            assertThat(shortViolations).anyMatch(v -> v.getMessage().contains("用户名长度必须在3-50字符之间"));

            // 用户名太长
            User longUsername = User.builder()
                    .username("a".repeat(51))
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> longViolations = validator.validate(longUsername);
            assertThat(longViolations).anyMatch(v -> v.getMessage().contains("用户名长度必须在3-50字符之间"));
        }

        @Test
        @DisplayName("手机号格式验证")
        void phoneFormatValidation() {
            // 无效手机号
            User invalidPhone = User.builder()
                    .username("testuser")
                    .phone("12345678901") // 不是有效的中国手机号
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(invalidPhone);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("手机号格式不正确"));
        }

        @Test
        @DisplayName("邮箱格式验证")
        void emailFormatValidation() {
            // 无效邮箱
            User invalidEmail = User.builder()
                    .username("testuser")
                    .email("invalid-email")
                    .passwordHash("hashedpassword123")
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(invalidEmail);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("邮箱格式不正确"));
        }

        @Test
        @DisplayName("密码不能为空")
        void passwordCannotBeNull() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash(null)
                    .role(UserRole.GUARDIAN)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("密码不能为空"));
        }

        @Test
        @DisplayName("角色不能为空")
        void roleCannotBeNull() {
            User user = User.builder()
                    .username("testuser")
                    .passwordHash("hashedpassword123")
                    .role(null)
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("用户角色不能为空"));
        }
    }

    @Nested
    @DisplayName("Elder实体验证测试")
    class ElderValidationTest {

        @Test
        @DisplayName("有效的Elder实体应通过验证")
        void validElderShouldPassValidation() {
            Elder elder = Elder.builder()
                    .name("张三")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1950, 1, 1))
                    .healthStatus("健康")
                    .privacyEnabled(true)
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("姓名不能为空")
        void nameCannotBeNull() {
            Elder elder = Elder.builder()
                    .name(null)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1950, 1, 1))
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("姓名不能为空"));
        }

        @Test
        @DisplayName("姓名长度必须在1-50字符之间")
        void nameSizeMustBeBetween1And50() {
            Elder longName = Elder.builder()
                    .name("张".repeat(51))
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1950, 1, 1))
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(longName);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("姓名长度必须在1-50字符之间"));
        }

        @Test
        @DisplayName("性别不能为空")
        void genderCannotBeNull() {
            Elder elder = Elder.builder()
                    .name("张三")
                    .gender(null)
                    .birthDate(LocalDate.of(1950, 1, 1))
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("性别不能为空"));
        }

        @Test
        @DisplayName("出生日期不能为空")
        void birthDateCannotBeNull() {
            Elder elder = Elder.builder()
                    .name("张三")
                    .gender(Gender.MALE)
                    .birthDate(null)
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("出生日期不能为空"));
        }

        @Test
        @DisplayName("出生日期必须是过去的日期")
        void birthDateMustBeInPast() {
            Elder elder = Elder.builder()
                    .name("张三")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.now().plusDays(1)) // 未来日期
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("出生日期必须是过去的日期"));
        }

        @Test
        @DisplayName("健康状况描述不能超过500字符")
        void healthStatusCannotExceed500Chars() {
            Elder elder = Elder.builder()
                    .name("张三")
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1950, 1, 1))
                    .healthStatus("健".repeat(501))
                    .build();

            Set<ConstraintViolation<Elder>> violations = validator.validate(elder);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("健康状况描述不能超过500字符"));
        }
    }

    @Nested
    @DisplayName("ElderGuardian实体验证测试")
    class ElderGuardianValidationTest {

        @Test
        @DisplayName("有效的ElderGuardian实体应通过验证")
        void validElderGuardianShouldPassValidation() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(1L)
                    .guardianId(1L)
                    .relationship("子女")
                    .authorized(true)
                    .build();

            Set<ConstraintViolation<ElderGuardian>> violations = validator.validate(eg);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("老人ID不能为空")
        void elderIdCannotBeNull() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(null)
                    .guardianId(1L)
                    .relationship("子女")
                    .build();

            Set<ConstraintViolation<ElderGuardian>> violations = validator.validate(eg);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("老人ID不能为空"));
        }

        @Test
        @DisplayName("监护人ID不能为空")
        void guardianIdCannotBeNull() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(1L)
                    .guardianId(null)
                    .relationship("子女")
                    .build();

            Set<ConstraintViolation<ElderGuardian>> violations = validator.validate(eg);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("监护人ID不能为空"));
        }

        @Test
        @DisplayName("关系描述不能为空")
        void relationshipCannotBeNull() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(1L)
                    .guardianId(1L)
                    .relationship(null)
                    .build();

            Set<ConstraintViolation<ElderGuardian>> violations = validator.validate(eg);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("关系描述不能为空"));
        }

        @Test
        @DisplayName("关系描述长度必须在1-50字符之间")
        void relationshipSizeMustBeBetween1And50() {
            ElderGuardian eg = ElderGuardian.builder()
                    .elderId(1L)
                    .guardianId(1L)
                    .relationship("子".repeat(51))
                    .build();

            Set<ConstraintViolation<ElderGuardian>> violations = validator.validate(eg);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("关系描述长度必须在1-50字符之间"));
        }
    }

    @Nested
    @DisplayName("EmotionRecord实体验证测试")
    class EmotionRecordValidationTest {

        @Test
        @DisplayName("有效的EmotionRecord实体应通过验证")
        void validEmotionRecordShouldPassValidation() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("老人ID不能为空")
        void elderIdCannotBeNull() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(null)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("老人ID不能为空"));
        }

        @Test
        @DisplayName("情感类型不能为空")
        void emotionTypeCannotBeNull() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(null)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("情感类型不能为空"));
        }

        @Test
        @DisplayName("置信度分数不能为空")
        void confidenceScoreCannotBeNull() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(null)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("置信度分数不能为空"));
        }

        @Test
        @DisplayName("置信度分数不能小于0.0")
        void confidenceScoreCannotBeBelowMin() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(-0.1)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("置信度分数不能小于0.0"));
        }

        @Test
        @DisplayName("置信度分数不能大于1.0")
        void confidenceScoreCannotExceedMax() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(1.1)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("置信度分数不能大于1.0"));
        }

        @Test
        @DisplayName("数据来源不能为空")
        void dataSourceCannotBeNull() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(null)
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("数据来源不能为空"));
        }

        @Test
        @DisplayName("分析时间不能为空")
        void analyzedAtCannotBeNull() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .analyzedAt(null)
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("分析时间不能为空"));
        }

        @Test
        @DisplayName("原始数据URL不能超过500字符")
        void rawDataUrlCannotExceed500Chars() {
            EmotionRecord record = EmotionRecord.builder()
                    .elderId(1L)
                    .emotionType(EmotionType.HAPPY)
                    .confidenceScore(0.95)
                    .dataSource(EmotionDataSource.VOICE)
                    .rawDataUrl("a".repeat(501))
                    .analyzedAt(LocalDateTime.now())
                    .build();

            Set<ConstraintViolation<EmotionRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("原始数据URL不能超过500字符"));
        }
    }

    @Nested
    @DisplayName("AlertRecord实体验证测试")
    class AlertRecordValidationTest {

        @Test
        @DisplayName("有效的AlertRecord实体应通过验证")
        void validAlertRecordShouldPassValidation() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("老人ID不能为空")
        void elderIdCannotBeNull() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(null)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("老人ID不能为空"));
        }

        @Test
        @DisplayName("预警类型不能为空")
        void alertTypeCannotBeNull() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(null)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("预警类型不能为空"));
        }

        @Test
        @DisplayName("严重程度不能为空")
        void severityCannotBeNull() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(null)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("严重程度不能为空"));
        }

        @Test
        @DisplayName("预警消息不能为空")
        void messageCannotBeNull() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message(null)
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("预警消息不能为空"));
        }

        @Test
        @DisplayName("预警消息长度必须在1-500字符之间")
        void messageSizeMustBeBetween1And500() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检".repeat(501))
                    .status(AlertStatus.PENDING)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("预警消息长度必须在1-500字符之间"));
        }

        @Test
        @DisplayName("处理状态不能为空")
        void statusCannotBeNull() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(null)
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("处理状态不能为空"));
        }

        @Test
        @DisplayName("处理备注不能超过1000字符")
        void handleNoteCannotExceed1000Chars() {
            AlertRecord record = AlertRecord.builder()
                    .elderId(1L)
                    .alertType(AlertType.NEGATIVE_EMOTION)
                    .severity(Severity.HIGH)
                    .message("检测到负面情绪")
                    .status(AlertStatus.PENDING)
                    .handleNote("备".repeat(1001))
                    .build();

            Set<ConstraintViolation<AlertRecord>> violations = validator.validate(record);
            assertThat(violations).anyMatch(v -> v.getMessage().contains("处理备注不能超过1000字符"));
        }
    }
}
