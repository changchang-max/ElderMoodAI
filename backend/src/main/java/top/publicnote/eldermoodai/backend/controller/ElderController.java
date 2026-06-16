package top.publicnote.eldermoodai.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import top.publicnote.eldermoodai.backend.dto.BindGuardianRequest;
import top.publicnote.eldermoodai.backend.dto.ElderRequest;
import top.publicnote.eldermoodai.backend.dto.ElderResponse;
import top.publicnote.eldermoodai.backend.exception.PermissionDeniedException;
import top.publicnote.eldermoodai.backend.exception.ResourceNotFoundException;
import top.publicnote.eldermoodai.backend.security.UserContext;
import top.publicnote.eldermoodai.backend.service.ElderService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/elders")
@RequiredArgsConstructor
public class ElderController {

    private final ElderService elderService;

    @PostMapping
    public ResponseEntity<?> createElder(@Valid @RequestBody ElderRequest request,
                                          @AuthenticationPrincipal UserContext userContext) {
        try {
            ElderResponse response = elderService.createElder(request, userContext.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateElder(@PathVariable Long id,
                                          @Valid @RequestBody ElderRequest request,
                                          @AuthenticationPrincipal UserContext userContext) {
        try {
            ElderResponse response = elderService.updateElder(id, request, userContext.getUserId());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getElderInfo(@PathVariable Long id,
                                           @AuthenticationPrincipal UserContext userContext) {
        try {
            ElderResponse response = elderService.getElderInfo(id, userContext.getUserId());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/guardians")
    public ResponseEntity<?> bindGuardian(@PathVariable Long id,
                                           @Valid @RequestBody BindGuardianRequest request,
                                           @AuthenticationPrincipal UserContext userContext) {
        try {
            elderService.bindGuardian(id, request, userContext.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "监护人绑定成功"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/privacy")
    public ResponseEntity<?> updatePrivacyStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, Boolean> body,
                                                  @AuthenticationPrincipal UserContext userContext) {
        try {
            Boolean privacyEnabled = body.get("privacyEnabled");
            ElderResponse response = elderService.updatePrivacyStatus(id, privacyEnabled, userContext.getUserId());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }
}
