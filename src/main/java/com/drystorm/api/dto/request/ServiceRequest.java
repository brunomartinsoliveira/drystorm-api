package com.drystorm.api.dto.request;

import com.drystorm.api.entity.Service;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceRequest {

    @NotBlank(message = "Nome do serviço é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Formato de preço inválido")
    private BigDecimal price;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 15, message = "Duração mínima é 15 minutos")
    @Max(value = 720, message = "Duração máxima é 720 minutos (12 horas)")
    private Integer durationMinutes;

    @NotNull(message = "Categoria é obrigatória")
    private Service.ServiceCategory category;
}
