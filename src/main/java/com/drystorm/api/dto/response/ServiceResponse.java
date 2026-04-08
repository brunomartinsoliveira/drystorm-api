package com.drystorm.api.dto.response;

import com.drystorm.api.entity.Service;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ServiceResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private String durationFormatted;
    private Service.ServiceCategory category;
    private String categoryLabel;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ServiceResponse from(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .durationFormatted(formatDuration(service.getDurationMinutes()))
                .category(service.getCategory())
                .categoryLabel(getCategoryLabel(service.getCategory()))
                .active(service.getActive())
                .createdAt(service.getCreatedAt())
                .build();
    }

    private static String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " min";
        int h = minutes / 60, m = minutes % 60;
        return m == 0 ? h + "h" : h + "h " + m + "min";
    }

    private static String getCategoryLabel(Service.ServiceCategory cat) {
        return switch (cat) {
            case CAMISETAS -> "Camisetas";
            case REGATAS -> "Regatas";
            case SHORTS -> "Shorts e bermudas";
            case ACESSORIOS -> "Acessórios";
            case KITS -> "Kits";
            case OUTROS -> "Outros";
        };
    }
}
