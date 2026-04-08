package com.drystorm.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String clientName;

    @NotBlank(message = "E-mail do cliente é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150)
    private String clientEmail;

    @NotBlank(message = "Telefone do cliente é obrigatório")
    @Pattern(regexp = "^[\\d\\s\\-\\(\\)\\+]{8,20}$", message = "Telefone inválido")
    private String clientPhone;

    @NotBlank(message = "Informe o produto ou preferência (ex.: tamanho e cor)")
    @Size(min = 2, max = 100, message = "Use entre 2 e 100 caracteres")
    private String clientProductNote;

    @NotNull(message = "Serviço é obrigatório")
    private Long serviceId;

    @NotNull(message = "Data do agendamento é obrigatória")
    @Future(message = "Data deve ser no futuro")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @NotNull(message = "Horário do agendamento é obrigatório")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String notes;
}
