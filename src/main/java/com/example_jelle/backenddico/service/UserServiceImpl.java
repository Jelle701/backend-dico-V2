package com.example_jelle.backenddico.service;

import com.example_jelle.backenddico.dto.onboarding.DeviceDto;
import com.example_jelle.backenddico.dto.onboarding.OnboardingRequestDto;
import com.example_jelle.backenddico.dto.user.FullUserProfileDto;
import com.example_jelle.backenddico.dto.UserDto;
import com.example_jelle.backenddico.exception.BadRequestException;
import com.example_jelle.backenddico.exception.EmailAlreadyExists;
import com.example_jelle.backenddico.exception.InvalidTokenException;
import com.example_jelle.backenddico.exception.UserNotFoundException;
import com.example_jelle.backenddico.model.*;
import com.example_jelle.backenddico.payload.request.RegisterRequest;
import com.example_jelle.backenddico.payload.request.VerifyRequest;
import com.example_jelle.backenddico.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserProfileRepository userProfileRepository, AccessCodeRepository accessCodeRepository, UserDeviceRepository userDeviceRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        String lowerCaseEmail = registerRequest.getEmail().toLowerCase();
        userRepository.findByEmail(lowerCaseEmail).ifPresent(u -> {
            throw new EmailAlreadyExists("This email address is already in use.");
        });
        User user = new User();
        user.setEmail(lowerCaseEmail);
        user.setUsername(lowerCaseEmail);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.PATIENT);
        user.setEnabled(false);
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpires(LocalDateTime.now().plusHours(24));
        logger.info("Generated verification code for {}: {}", user.getEmail(), verificationCode);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyUser(VerifyRequest verifyRequest) {
        String token = verifyRequest.getToken();
        User user = userRepository.findByVerificationCode(token)
                .orElseThrow(() -> new InvalidTokenException(String.format("Invalid verification code: '%s'", token)));
        if (user.getVerificationCodeExpires().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification code has expired.");
        }
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpires(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void saveProfileDetails(String username, OnboardingRequestDto onboardingRequestDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        Role newRole;
        try {
            newRole = Role.valueOf(onboardingRequestDto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid role specified: " + onboardingRequestDto.getRole());
        }

        if (newRole == Role.PATIENT) {
            if (onboardingRequestDto.getGender() == null) {
                throw new BadRequestException("Gender is required for PATIENT role.");
            }
            if (onboardingRequestDto.getDiabetesType() == null) {
                throw new BadRequestException("Diabetes type is required for PATIENT role.");
            }
        }

        user.setRole(newRole);
        user.setFirstName(onboardingRequestDto.getFirstName());
        user.setLastName(onboardingRequestDto.getLastName());
        user.setDob(onboardingRequestDto.getDateOfBirth());

        UserProfile userProfile = user.getUserProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
        }

        userProfile.setLength(onboardingRequestDto.getHeight());
        userProfile.setWeight(onboardingRequestDto.getWeight());
        userProfile.setGender(onboardingRequestDto.getGender());
        userProfile.setDiabetesType(onboardingRequestDto.getDiabetesType());
        userProfile.setLongActingInsulin(onboardingRequestDto.getLongActingInsulin());
        userProfile.setShortActingInsulin(onboardingRequestDto.getShortActingInsulin());

        userDeviceRepository.deleteAllByUser(user);
        if (onboardingRequestDto.getDiabeticDevices() != null) {
            for (DeviceDto deviceDto : onboardingRequestDto.getDiabeticDevices()) {
                UserDevice device = new UserDevice();
                device.setUser(user);
                device.setCategory(deviceDto.getCategory().name());
                device.setManufacturer(deviceDto.getBrand());
                device.setModel(deviceDto.getModel());
                userDeviceRepository.save(device);
            }
        }

        user.getFlags().setHasDetails(true);
        userRepository.save(user);
        logger.info("Saved profile details for user: {}", username);
    }

    @Override
    public FullUserProfileDto getFullUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return new FullUserProfileDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(this::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public String generateAccessCode(String patientEmail) {
        User user = userRepository.findByUsername(patientEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + patientEmail));

        List<AccessCode> existingCodes = accessCodeRepository.findAllByUserAndExpirationTimeAfter(user, LocalDateTime.now());
        if (!existingCodes.isEmpty()) {
            logger.info("Invalidating {} existing access code(s) for user {}.", existingCodes.size(), patientEmail);
            for (AccessCode oldCode : existingCodes) {
                oldCode.setExpirationTime(LocalDateTime.now());
            }
            accessCodeRepository.saveAllAndFlush(existingCodes);
            logger.info("Successfully flushed invalidation of old codes.");
        }

        String code = String.format("%s-%s-%s",
            UUID.randomUUID().toString().substring(0, 3).toUpperCase(),
            UUID.randomUUID().toString().substring(0, 3).toUpperCase(),
            UUID.randomUUID().toString().substring(0, 3).toUpperCase());

        AccessCode accessCode = new AccessCode();
        accessCode.setUser(user);
        accessCode.setCode(code);
        accessCode.setCreationTime(LocalDateTime.now());
        accessCode.setExpirationTime(LocalDateTime.now().plusHours(24));

        accessCodeRepository.save(accessCode);
        logger.info("Generated and saved new access code {} for user {}.", code, patientEmail);
        return code;
    }

    @Override
    public String getAccessCode(String patientEmail) {
        User user = userRepository.findByUsername(patientEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + patientEmail));

        return accessCodeRepository.findByUserIdAndExpirationTimeAfter(user.getId(), LocalDateTime.now())
                .map(AccessCode::getCode)
                .orElse(null);
    }

    private UserDto fromUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
