package com.drystorm.api.dto.request;

import com.drystorm.api.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private Appointment.AppointmentStatus status;

    private String notes;
}
