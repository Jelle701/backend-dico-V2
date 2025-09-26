package com.example_jelle.backenddico.dto.onboarding;

import com.example_jelle.backenddico.model.enums.DiabetesType;
import com.example_jelle.backenddico.model.enums.Gender;
import com.example_jelle.backenddico.model.enums.InsulinType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class OnboardingRequestDto {

    // New fields from frontend
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    private double height;
    private double weight;

    // @NotNull removed to allow conditional validation in the service layer
    private Gender gender;

    // @NotNull removed to allow conditional validation in the service layer
    private DiabetesType diabetesType;

    private InsulinType longActingInsulin;
    private InsulinType shortActingInsulin;

    @Valid
    private List<DeviceDto> diabeticDevices;

    @NotBlank(message = "Role is required.")
    private String role;

    // Getters and Setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public DiabetesType getDiabetesType() {
        return diabetesType;
    }

    public void setDiabetesType(DiabetesType diabetesType) {
        this.diabetesType = diabetesType;
    }

    public InsulinType getLongActingInsulin() {
        return longActingInsulin;
    }

    public void setLongActingInsulin(InsulinType longActingInsulin) {
        this.longActingInsulin = longActingInsulin;
    }

    public InsulinType getShortActingInsulin() {
        return shortActingInsulin;
    }

    public void setShortActingInsulin(InsulinType shortActingInsulin) {
        this.shortActingInsulin = shortActingInsulin;
    }

    public List<DeviceDto> getDiabeticDevices() {
        return diabeticDevices;
    }

    public void setDiabeticDevices(List<DeviceDto> diabeticDevices) {
        this.diabeticDevices = diabeticDevices;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
