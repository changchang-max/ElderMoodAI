package top.publicnote.eldermoodai.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.publicnote.eldermoodai.backend.dto.BindGuardianRequest;
import top.publicnote.eldermoodai.backend.dto.ElderRequest;
import top.publicnote.eldermoodai.backend.dto.ElderResponse;
import top.publicnote.eldermoodai.backend.entity.Elder;
import top.publicnote.eldermoodai.backend.entity.ElderGuardian;
import top.publicnote.eldermoodai.backend.entity.User;
import top.publicnote.eldermoodai.backend.enums.UserRole;
import top.publicnote.eldermoodai.backend.exception.OptimisticLockingFailureException;
import top.publicnote.eldermoodai.backend.exception.PermissionDeniedException;
import top.publicnote.eldermoodai.backend.exception.ResourceNotFoundException;
import top.publicnote.eldermoodai.backend.exception.DuplicateResourceException;
import top.publicnote.eldermoodai.backend.repository.ElderGuardianRepository;
import top.publicnote.eldermoodai.backend.repository.ElderRepository;
import top.publicnote.eldermoodai.backend.repository.UserRepository;
import top.publicnote.eldermoodai.backend.service.AuditLoggingService;
import top.publicnote.eldermoodai.backend.service.ElderService;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElderServiceImpl implements ElderService {

    private static final String CACHE_KEY_PREFIX = "elder:cache:";
    private static final long CACHE_TTL_HOURS = 1;
    private static final int MIN_ELDER_AGE = 60;
    private static final int MAX_ELDER_AGE = 120;

    private final ElderRepository elderRepository;
    private final ElderGuardianRepository elderGuardianRepository;
    private final UserRepository userRepository;
    private final AuditLoggingService auditLoggingService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public ElderResponse createElder(ElderRequest request, Long operatorId) {
        UserRole role = getOperatorRole(operatorId);
        if (role != UserRole.ADMIN && role != UserRole.CAREGIVER) {
            throw new PermissionDeniedException("无权创建老人档案");
        }

        int age = calculateAge(request.getBirthDate());
        if (age < MIN_ELDER_AGE || age > MAX_ELDER_AGE) {
            throw new IllegalArgumentException("老人年龄必须在" + MIN_ELDER_AGE + "-" + MAX_ELDER_AGE + "岁之间");
        }

        Elder elder = Elder.builder()
                .name(request.getName().trim())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .healthStatus(request.getHealthStatus())
                .privacyEnabled(request.getPrivacyEnabled() != null ? request.getPrivacyEnabled() : true)
                .build();

        elder = elderRepository.save(elder);

        auditLoggingService.log(operatorId, "CREATE_ELDER", "ELDER", elder.getId(), null,
                "{\"name\":\"" + elder.getName() + "\"}");

        log.info("Elder created: elderId={}, operatorId={}", elder.getId(), operatorId);

        return toResponse(elder);
    }

    @Override
    @Transactional
    public ElderResponse updateElder(Long id, ElderRequest request, Long operatorId) {
        UserRole role = getOperatorRole(operatorId);
        if (role != UserRole.ADMIN && role != UserRole.CAREGIVER) {
            throw new PermissionDeniedException("无权修改老人档案");
        }

        Elder elder = elderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("老人不存在"));

        if (request.getVersion() == null) {
            throw new IllegalArgumentException("版本号不能为空");
        }
        if (!request.getVersion().equals(elder.getVersion())) {
            throw new OptimisticLockingFailureException("数据已被其他操作修改，请刷新后重试");
        }

        elder.setName(request.getName().trim());
        elder.setGender(request.getGender());
        elder.setBirthDate(request.getBirthDate());
        elder.setHealthStatus(request.getHealthStatus());
        if (request.getPrivacyEnabled() != null) {
            elder.setPrivacyEnabled(request.getPrivacyEnabled());
        }

        elder = elderRepository.save(elder);

        evictCache(id);

        auditLoggingService.log(operatorId, "UPDATE_ELDER", "ELDER", id, null,
                "{\"version\":" + request.getVersion() + "}");

        log.info("Elder updated: elderId={}, operatorId={}", id, operatorId);

        return toResponse(elder);
    }

    @Override
    @Transactional(readOnly = true)
    public ElderResponse getElderInfo(Long id, Long operatorId) {
        UserRole role = getOperatorRole(operatorId);

        Optional<ElderResponse> cached = getFromCache(id);
        if (cached.isPresent()) {
            ElderResponse response = cached.get();
            checkViewPermission(id, operatorId, role, response.getPrivacyEnabled());
            return response;
        }

        Elder elder = elderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("老人不存在"));

        checkViewPermission(id, operatorId, role, elder.getPrivacyEnabled());

        ElderResponse response = toResponse(elder);
        putInCache(id, response);

        auditLoggingService.log(operatorId, "VIEW_ELDER", "ELDER", id, null, null);

        return response;
    }

    @Override
    @Transactional
    public void bindGuardian(Long elderId, BindGuardianRequest request, Long operatorId) {
        UserRole role = getOperatorRole(operatorId);
        if (role != UserRole.ADMIN && role != UserRole.CAREGIVER) {
            throw new PermissionDeniedException("无权绑定监护人");
        }

        if (!elderRepository.existsById(elderId)) {
            throw new ResourceNotFoundException("老人不存在");
        }

        if (elderGuardianRepository.findByElderIdAndGuardianId(elderId, request.getGuardianId()).isPresent()) {
            throw new DuplicateResourceException("该监护人已绑定此老人");
        }

        ElderGuardian relation = ElderGuardian.builder()
                .elderId(elderId)
                .guardianId(request.getGuardianId())
                .relationship("监护人")
                .authorized(true)
                .build();

        elderGuardianRepository.save(relation);

        auditLoggingService.log(operatorId, "BIND_GUARDIAN", "ELDER", elderId, null,
                "{\"guardianId\":" + request.getGuardianId() + "}");

        log.info("Guardian bound: elderId={}, guardianId={}, operatorId={}",
                elderId, request.getGuardianId(), operatorId);
    }

    @Override
    @Transactional
    public ElderResponse updatePrivacyStatus(Long id, Boolean privacyEnabled, Long operatorId) {
        UserRole role = getOperatorRole(operatorId);

        Elder elder = elderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("老人不存在"));

        if (role == UserRole.CAREGIVER) {
            throw new PermissionDeniedException("无权修改隐私保护状态");
        }
        if (role == UserRole.GUARDIAN) {
            boolean isBound = elderGuardianRepository.findByElderIdAndGuardianId(id, operatorId).isPresent();
            if (!isBound) {
                throw new PermissionDeniedException("无权修改该老人的隐私保护状态");
            }
        }

        if (privacyEnabled != null) {
            elder.setPrivacyEnabled(privacyEnabled);
        }

        elder = elderRepository.save(elder);

        evictCache(id);

        auditLoggingService.log(operatorId, "UPDATE_PRIVACY", "ELDER", id, null,
                "{\"privacyEnabled\":" + elder.getPrivacyEnabled() + "}");

        log.info("Privacy status updated: elderId={}, privacyEnabled={}, operatorId={}",
                id, elder.getPrivacyEnabled(), operatorId);

        return toResponse(elder);
    }

    private UserRole getOperatorRole(Long operatorId) {
        return userRepository.findById(operatorId)
                .map(User::getRole)
                .orElseThrow(() -> new PermissionDeniedException("用户不存在或未登录"));
    }

    private void checkViewPermission(Long elderId, Long operatorId, UserRole role, Boolean privacyEnabled) {
        if (role == UserRole.ADMIN) {
            return;
        }
        if (role == UserRole.CAREGIVER && Boolean.FALSE.equals(privacyEnabled)) {
            return;
        }
        if (role == UserRole.CAREGIVER && Boolean.TRUE.equals(privacyEnabled)) {
            throw new PermissionDeniedException("老人已开启隐私保护，无法查看");
        }
        if (role == UserRole.GUARDIAN) {
            boolean isBound = elderGuardianRepository.findByElderIdAndGuardianId(elderId, operatorId).isPresent();
            if (isBound) {
                return;
            }
        }
        throw new PermissionDeniedException("无权查看该老人信息");
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private ElderResponse toResponse(Elder elder) {
        return ElderResponse.builder()
                .id(elder.getId())
                .name(elder.getName())
                .gender(elder.getGender())
                .birthDate(elder.getBirthDate())
                .healthStatus(elder.getHealthStatus())
                .privacyEnabled(elder.getPrivacyEnabled())
                .createdAt(elder.getCreatedAt())
                .updatedAt(elder.getUpdatedAt())
                .build();
    }

    private Optional<ElderResponse> getFromCache(Long elderId) {
        String key = CACHE_KEY_PREFIX + elderId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        // In a full implementation, deserialize JSON to ElderResponse
        // Using StringRedisTemplate for cache invalidation is the primary mechanism
        return Optional.empty();
    }

    private void putInCache(Long elderId, ElderResponse response) {
        String key = CACHE_KEY_PREFIX + elderId;
        String json = response.getId() + ":" + response.getName();
        redisTemplate.opsForValue().set(key, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    private void evictCache(Long elderId) {
        String key = CACHE_KEY_PREFIX + elderId;
        redisTemplate.delete(key);
    }
}
