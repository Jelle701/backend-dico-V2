package com.example_jelle.backenddico.controller;

import com.example_jelle.backenddico.dto.guardian.LinkPatientRequestDto;
import com.example_jelle.backenddico.dto.health.HealthDataResponse;
import com.example_jelle.backenddico.dto.provider.DelegatedTokenResponseDto;
import com.example_jelle.backenddico.dto.provider.PatientSummaryDto;
import com.example_jelle.backenddico.dto.user.FullUserProfileDto;
import com.example_jelle.backenddico.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping("/link-patient")
    public ResponseEntity<Void> linkPatient(Authentication authentication, @Valid @RequestBody LinkPatientRequestDto request) {
        String providerUsername = authentication.getName();
        providerService.linkPatient(providerUsername, request.getAccessCode());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patients")
    public ResponseEntity<List<FullUserProfileDto>> getLinkedPatients(Authentication authentication) {
        String providerUsername = authentication.getName();
        List<FullUserProfileDto> patients = providerService.getLinkedPatients(providerUsername);
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/patients/{patientId}/delegate-token")
    public ResponseEntity<DelegatedTokenResponseDto> createDelegateToken(
            Authentication authentication,
            @PathVariable Long patientId) {
        String providerUsername = authentication.getName();
        DelegatedTokenResponseDto response = providerService.createDelegateToken(providerUsername, patientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{patientId}/dashboard-summary")
    public ResponseEntity<HealthDataResponse> getPatientDashboardSummary(
            Authentication authentication,
            @PathVariable Long patientId) {
        String providerUsername = authentication.getName();
        HealthDataResponse summary = providerService.getPatientDashboardSummary(providerUsername, patientId);
        return ResponseEntity.ok(summary);
    }

    /**
     * NEW: Retrieves an aggregated summary of all patients linked to the provider.
     * @param authentication The authentication object for the current provider.
     * @return A ResponseEntity containing a list of patient summaries.
     */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<List<PatientSummaryDto>> getDashboardSummary(Authentication authentication) {
        String providerUsername = authentication.getName();
        List<PatientSummaryDto> summary = providerService.getDashboardSummary(providerUsername);
        return ResponseEntity.ok(summary);
    }
}
