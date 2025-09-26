package com.example_jelle.backenddico.service;

import com.example_jelle.backenddico.dto.GlucoseMeasurementDto;
import com.example_jelle.backenddico.dto.health.HealthDataResponse;
import com.example_jelle.backenddico.dto.provider.DelegatedTokenResponseDto;
import com.example_jelle.backenddico.dto.provider.PatientSummaryDto;
import com.example_jelle.backenddico.dto.user.FullUserProfileDto;
import com.example_jelle.backenddico.exception.InvalidAccessException;
import com.example_jelle.backenddico.exception.UserNotFoundException;
import com.example_jelle.backenddico.model.AccessCode;
import com.example_jelle.backenddico.model.GlucoseMeasurement;
import com.example_jelle.backenddico.model.User;
import com.example_jelle.backenddico.repository.AccessCodeRepository;
import com.example_jelle.backenddico.repository.GlucoseMeasurementRepository;
import com.example_jelle.backenddico.repository.UserRepository;
import com.example_jelle.backenddico.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final UserRepository userRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final HealthDataService healthDataService;
    private final GlucoseMeasurementRepository glucoseMeasurementRepository;

    public ProviderServiceImpl(UserRepository userRepository, AccessCodeRepository accessCodeRepository, UserService userService, JwtUtil jwtUtil, UserDetailsService userDetailsService, HealthDataService healthDataService, GlucoseMeasurementRepository glucoseMeasurementRepository) {
        this.userRepository = userRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.healthDataService = healthDataService;
        this.glucoseMeasurementRepository = glucoseMeasurementRepository;
    }

    @Override
    @Transactional
    public void linkPatient(String providerUsername, String accessCode) {
        User provider = userRepository.findByUsername(providerUsername)
                .orElseThrow(() -> new UserNotFoundException("Provider not found: " + providerUsername));

        AccessCode code = accessCodeRepository.findByCodeAndExpirationTimeAfter(accessCode, LocalDateTime.now())
                .orElseThrow(() -> new InvalidAccessException("Access code is invalid or expired."));
        User patient = code.getUser();

        provider.getLinkedPatients().add(patient);
        userRepository.save(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FullUserProfileDto> getLinkedPatients(String providerUsername) {
        User provider = userRepository.findByUsername(providerUsername)
                .orElseThrow(() -> new UserNotFoundException("Provider not found: " + providerUsername));

        return provider.getLinkedPatients().stream()
                .map(patient -> userService.getFullUserProfile(patient.getUsername()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DelegatedTokenResponseDto createDelegateToken(String providerUsername, Long patientId) {
        User provider = userRepository.findByUsername(providerUsername)
                .orElseThrow(() -> new UserNotFoundException("Provider not found: " + providerUsername));

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with ID: " + patientId));

        boolean isLinked = provider.getLinkedPatients().stream().anyMatch(p -> p.getId().equals(patientId));
        if (!isLinked) {
            throw new InvalidAccessException("Provider is not linked to the specified patient.");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(patient.getUsername());
        final String delegatedToken = jwtUtil.generateDelegatedToken(patient.getUsername());

        return new DelegatedTokenResponseDto(delegatedToken, patient.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthDataResponse getPatientDashboardSummary(String providerUsername, Long patientId) {
        User provider = userRepository.findByUsername(providerUsername)
                .orElseThrow(() -> new UserNotFoundException("Provider not found: " + providerUsername));

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with ID: " + patientId));

        boolean isLinked = provider.getLinkedPatients().stream().anyMatch(p -> p.getId().equals(patientId));
        if (!isLinked) {
            throw new InvalidAccessException("Provider is not authorized to view this patient's data.");
        }

        return healthDataService.getHealthDataSummary(patient.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getDashboardSummary(String providerUsername) {
        User provider = userRepository.findByUsername(providerUsername)
                .orElseThrow(() -> new UserNotFoundException("Provider not found: " + providerUsername));

        Set<User> linkedPatients = provider.getLinkedPatients();

        return linkedPatients.stream()
                .map(patient -> {
                    GlucoseMeasurement latestMeasurement = glucoseMeasurementRepository
                            .findTopByUserIdOrderByTimestampDesc(patient.getId())
                            .orElse(null);

                    // FIX: Use the static factory method fromEntity() instead of a constructor
                    GlucoseMeasurementDto latestMeasurementDto = (latestMeasurement != null)
                            ? GlucoseMeasurementDto.fromEntity(latestMeasurement)
                            : null;

                    return new PatientSummaryDto(
                            patient.getId(),
                            patient.getFirstName(),
                            patient.getLastName(),
                            latestMeasurementDto
                    );
                })
                .collect(Collectors.toList());
    }
}
