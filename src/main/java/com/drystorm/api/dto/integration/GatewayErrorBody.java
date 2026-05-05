package com.drystorm.api.dto.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayErrorBody {
    private String code;
    private String message;
    private String detail;
    private Map<String, String> fields;
    private LocalDateTime timestamp;
}
