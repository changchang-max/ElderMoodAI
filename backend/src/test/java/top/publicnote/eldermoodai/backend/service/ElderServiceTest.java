package top.publicnote.eldermoodai.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import top.publicnote.eldermoodai.backend.dto.BindGuardianRequest;
import top.publicnote.eldermoodai.backend.dto.ElderRequest;
import top.publicnote.eldermoodai.backend.dto.ElderResponse;
import top.publicnote.eldermoodai.backend.entity.Elder;
import top.publicnote.eldermoodai.backend.entity.ElderGuardian;
import top.publicnote.eldermoodai.backend.entity.User;
import top.publicnote.eldermoodai.backend.enums.Gender;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.exception.DuplicateResourceException;
import top.publicnote.eldermoodai.backend.exception.OptimisticLockingFailureException;
import top.publicnote.eldermoodai.backend.exception.PermissionDeniedException;
import top.publicnote.eldermoodai.backend.exception.ResourceNotFoundException;
import top.publicnote.eldermoodai.backend.repository.ElderGuardianRepository;
import top.publicnote.eldermoodai.backend.repository.ElderRepository;
import top.publicnote.eldermoodai.backend.repository.UserRepository;
import top.publicnote.eldermoodai.backend.service.impl.ElderServiceImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElderServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private ElderGuardianRepository elderGuardianRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLoggingService auditLoggingService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ElderServiceImpl elderService;

    private User adminUser;
    private User caregiverUser;
    private User guardianUser;
    private Elder testElder;
    private ElderRequest validRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(UserRole.ADMIN)
                .build();

        caregiverUser = User.builder()
                .id(2L)
                .username("caregiver")
                .role(UserRole.CAREGIVER)
                .build();

        guardianUser = User.builder()
                .id(3L)
                .username("guardian")
                .role(UserRole.GUARDIAN)
                .build();

        testElder = Elder.builder()
                .id(10L)
                .name("张三")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1950, 1, 1))
                .healthStatus("良好")
                .privacyEnabled(true)
                .version(1L)
                .build();

        validRequest = ElderRequest.builder()
                .name("李四")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1940, 5, 15))
                .healthStatus("一般")
                .privacyEnabled(false)
                .build();
    }

    // ─── createElder ───────────────────────────────────────────────────

    @Test
    void createElder_ByAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.save(any(Elder.class))).thenAnswer(invocation -> {
            Elder e = invocation.getArgument(0);
            e.setId(20L);
            return e;
        });

        ElderResponse response = elderService.createElder(validRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getName()).isEqualTo("李四");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getPrivacyEnabled()).isFalse();

        verify(auditLoggingService).log(eq(1L), eq("CREATE_ELDER"), eq("ELDER"), eq(20L), isNull(), anyString());
    }

    @Test
    void createElder_ByCaregiver_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(caregiverUser));
        when(elderRepository.save(any(Elder.class))).thenAnswer(invocation -> {
            Elder e = invocation.getArgument(0);
            e.setId(21L);
            return e;
        });

        ElderResponse response = elderService.createElder(validRequest, 2L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(21L);
    }

    @Test
    void createElder_ByGuardian_ThrowsPermissionDenied() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));

        assertThatThrownBy(() -> elderService.createElder(validRequest, 3L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权创建老人档案");
    }

    @Test
    void createElder_WithInvalidAge_ThrowsIllegalArgument() {
        ElderRequest youngRequest = ElderRequest.builder()
                .name("年轻")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> elderService.createElder(youngRequest, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("年龄必须在60-120岁之间");
    }

    // ─── updateElder ───────────────────────────────────────────────────

    @Test
    void updateElder_ByAdmin_Success() {
        ElderRequest updateRequest = ElderRequest.builder()
                .name("王五")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1955, 3, 10))
                .healthStatus("优秀")
                .privacyEnabled(false)
                .version(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderRepository.save(any(Elder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ElderResponse response = elderService.updateElder(10L, updateRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("王五");
        assertThat(response.getHealthStatus()).isEqualTo("优秀");

        verify(redisTemplate).delete("elder:cache:10");
        verify(auditLoggingService).log(eq(1L), eq("UPDATE_ELDER"), eq("ELDER"), eq(10L), isNull(), anyString());
    }

    @Test
    void updateElder_ByGuardian_ThrowsPermissionDenied() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));

        assertThatThrownBy(() -> elderService.updateElder(10L, validRequest, 3L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权修改老人档案");
    }

    @Test
    void updateElder_WithNullVersion_ThrowsIllegalArgument() {
        ElderRequest noVersion = ElderRequest.builder()
                .name("王五")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1955, 3, 10))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));

        assertThatThrownBy(() -> elderService.updateElder(10L, noVersion, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("版本号不能为空");
    }

    @Test
    void updateElder_WithVersionMismatch_ThrowsOptimisticLockingFailure() {
        ElderRequest wrongVersion = ElderRequest.builder()
                .name("王五")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1955, 3, 10))
                .version(99L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));

        assertThatThrownBy(() -> elderService.updateElder(10L, wrongVersion, 1L))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("数据已被其他操作修改");
    }

    @Test
    void updateElder_NotFound_ThrowsResourceNotFound() {
        ElderRequest updateRequest = ElderRequest.builder()
                .name("王五")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1955, 3, 10))
                .version(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> elderService.updateElder(99L, updateRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("老人不存在");
    }

    // ─── getElderInfo ──────────────────────────────────────────────────

    @Test
    void getElderInfo_ByAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:10")).thenReturn(null);
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));

        ElderResponse response = elderService.getElderInfo(10L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("张三");
        assertThat(response.getAge()).isGreaterThanOrEqualTo(76);

        verify(auditLoggingService).log(eq(1L), eq("VIEW_ELDER"), eq("ELDER"), eq(10L), isNull(), isNull());
    }

    @Test
    void getElderInfo_ByCaregiverWithPrivacyDisabled_Success() {
        Elder elderNoPrivacy = Elder.builder()
                .id(11L)
                .name("赵六")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(1945, 7, 7))
                .privacyEnabled(false)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(caregiverUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:11")).thenReturn(null);
        when(elderRepository.findById(11L)).thenReturn(Optional.of(elderNoPrivacy));

        ElderResponse response = elderService.getElderInfo(11L, 2L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("赵六");
    }

    @Test
    void getElderInfo_ByCaregiverWithPrivacyEnabled_ThrowsPermissionDenied() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(caregiverUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:10")).thenReturn(null);
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));

        assertThatThrownBy(() -> elderService.getElderInfo(10L, 2L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("老人已开启隐私保护");
    }

    @Test
    void getElderInfo_ByGuardianBound_Success() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:10")).thenReturn(null);
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L))
                .thenReturn(Optional.of(new ElderGuardian()));

        ElderResponse response = elderService.getElderInfo(10L, 3L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("张三");
    }

    @Test
    void getElderInfo_ByGuardianNotBound_ThrowsPermissionDenied() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:10")).thenReturn(null);
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> elderService.getElderInfo(10L, 3L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权查看该老人信息");
    }

    @Test
    void getElderInfo_NotFound_ThrowsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("elder:cache:99")).thenReturn(null);
        when(elderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> elderService.getElderInfo(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("老人不存在");
    }

    // ─── bindGuardian ──────────────────────────────────────────────────

    @Test
    void bindGuardian_ByAdmin_Success() {
        BindGuardianRequest request = BindGuardianRequest.builder()
                .elderId(10L)
                .guardianId(3L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.existsById(10L)).thenReturn(true);
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L)).thenReturn(Optional.empty());

        elderService.bindGuardian(10L, request, 1L);

        verify(elderGuardianRepository).save(any(ElderGuardian.class));
        verify(auditLoggingService).log(eq(1L), eq("BIND_GUARDIAN"), eq("ELDER"), eq(10L), isNull(), anyString());
    }

    @Test
    void bindGuardian_ByGuardian_ThrowsPermissionDenied() {
        BindGuardianRequest request = BindGuardianRequest.builder()
                .elderId(10L)
                .guardianId(4L)
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));

        assertThatThrownBy(() -> elderService.bindGuardian(10L, request, 3L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权绑定监护人");
    }

    @Test
    void bindGuardian_ElderNotFound_ThrowsResourceNotFound() {
        BindGuardianRequest request = BindGuardianRequest.builder()
                .elderId(99L)
                .guardianId(3L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> elderService.bindGuardian(99L, request, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("老人不存在");
    }

    @Test
    void bindGuardian_AlreadyBound_ThrowsDuplicateResource() {
        BindGuardianRequest request = BindGuardianRequest.builder()
                .elderId(10L)
                .guardianId(3L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.existsById(10L)).thenReturn(true);
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L))
                .thenReturn(Optional.of(new ElderGuardian()));

        assertThatThrownBy(() -> elderService.bindGuardian(10L, request, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("该监护人已绑定此老人");
    }

    // ─── updatePrivacyStatus ───────────────────────────────────────────

    @Test
    void updatePrivacyStatus_ByAdmin_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderRepository.save(any(Elder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ElderResponse response = elderService.updatePrivacyStatus(10L, false, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getPrivacyEnabled()).isFalse();

        verify(redisTemplate).delete("elder:cache:10");
        verify(auditLoggingService).log(eq(1L), eq("UPDATE_PRIVACY"), eq("ELDER"), eq(10L), isNull(), anyString());
    }

    @Test
    void updatePrivacyStatus_ByBoundGuardian_Success() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L))
                .thenReturn(Optional.of(new ElderGuardian()));
        when(elderRepository.save(any(Elder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ElderResponse response = elderService.updatePrivacyStatus(10L, true, 3L);

        assertThat(response).isNotNull();
        assertThat(response.getPrivacyEnabled()).isTrue();
    }

    @Test
    void updatePrivacyStatus_ByCaregiver_ThrowsPermissionDenied() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(caregiverUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));

        assertThatThrownBy(() -> elderService.updatePrivacyStatus(10L, false, 2L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权修改隐私保护状态");
    }

    @Test
    void updatePrivacyStatus_ByUnboundGuardian_ThrowsPermissionDenied() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(guardianUser));
        when(elderRepository.findById(10L)).thenReturn(Optional.of(testElder));
        when(elderGuardianRepository.findByElderIdAndGuardianId(10L, 3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> elderService.updatePrivacyStatus(10L, false, 3L))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("无权修改该老人的隐私保护状态");
    }

    @Test
    void updatePrivacyStatus_ElderNotFound_ThrowsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(elderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> elderService.updatePrivacyStatus(99L, false, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("老人不存在");
    }
}
