package com.example_jelle.backenddico.dto.provider;

import com.example_jelle.backenddico.dto.GlucoseMeasurementDto;

public class PatientSummaryDto {
    private Long patientId;
    private String firstName;
    private String lastName;
    private GlucoseMeasurementDto latestGlucoseMeasurement;

    public PatientSummaryDto(Long patientId, String firstName, String lastName, GlucoseMeasurementDto latestGlucoseMeasurement) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.latestGlucoseMeasurement = latestGlucoseMeasurement;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

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

    public GlucoseMeasurementDto getLatestGlucoseMeasurement() {
        return latestGlucoseMeasurement;
    }

    public void setLatestGlucoseMeasurement(GlucoseMeasurementDto latestGlucoseMeasurement) {
        this.latestGlucoseMeasurement = latestGlucoseMeasurement;
    }
}
