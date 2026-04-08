package com.drystorm.api.dto.response;

import com.drystorm.api.entity.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {

    private Long id;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientProductNote;
    private ServiceResponse service;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Appointment.AppointmentStatus status;
    private String statusLabel;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static AppointmentResponse from(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .clientName(a.getClientName())
                .clientEmail(a.getClientEmail())
                .clientPhone(a.getClientPhone())
                .clientProductNote(a.getClientProductNote())
                .service(ServiceResponse.from(a.getService()))
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .statusLabel(getStatusLabel(a.getStatus()))
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private static String getStatusLabel(Appointment.AppointmentStatus status) {
        return switch (status) {
            case PENDING -> "Aguardando Confirmação";
            case CONFIRMED -> "Confirmado";
            case IN_PROGRESS -> "Em Andamento";
            case COMPLETED -> "Concluído";
            case CANCELLED -> "Cancelado";
            case NO_SHOW -> "Não Compareceu";
        };
    }
}
