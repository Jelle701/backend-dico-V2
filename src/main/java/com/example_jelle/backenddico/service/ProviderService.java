package com.example_jelle.backenddico.service;

import com.example_jelle.backenddico.dto.health.HealthDataResponse;
import com.example_jelle.backenddico.dto.provider.DelegatedTokenResponseDto;
import com.example_jelle.backenddico.dto.provider.PatientSummaryDto;
import com.example_jelle.backenddico.dto.user.FullUserProfileDto;
import java.util.List;

public interface ProviderService {

    void linkPatient(String providerUsername, String accessCode);

    List<FullUserProfileDto> getLinkedPatients(String providerUsername);

    DelegatedTokenResponseDto createDelegateToken(String providerUsername, Long patientId);

    HealthDataResponse getPatientDashboardSummary(String providerUsername, Long patientId);

    /**
     * Retrieves an aggregated summary of all patients linked to a provider.
     * @param providerUsername The username of the provider.
     * @return A list of PatientSummaryDto objects.
     */
    List<PatientSummaryDto> getDashboardSummary(String providerUsername);
}
