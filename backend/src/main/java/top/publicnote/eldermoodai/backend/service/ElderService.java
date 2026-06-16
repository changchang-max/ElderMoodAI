package top.publicnote.eldermoodai.backend.service;

import top.publicnote.eldermoodai.backend.dto.BindGuardianRequest;
import top.publicnote.eldermoodai.backend.dto.ElderRequest;
import top.publicnote.eldermoodai.backend.dto.ElderResponse;

public interface ElderService {

    ElderResponse createElder(ElderRequest request, Long operatorId);

    ElderResponse updateElder(Long id, ElderRequest request, Long operatorId);

    ElderResponse getElderInfo(Long id, Long operatorId);

    void bindGuardian(Long elderId, BindGuardianRequest request, Long operatorId);

    ElderResponse updatePrivacyStatus(Long id, Boolean privacyEnabled, Long operatorId);
}
