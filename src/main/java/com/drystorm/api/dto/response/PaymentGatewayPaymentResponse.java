package com.drystorm.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resposta do payment-gateway (deserialização do JSON retornado).
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentGatewayPaymentResponse {

    private UUID id;
    private String idempotencyKey;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String cardLastFour;
    private String cardBrand;
    private String description;
    private String status;
    private String statusDescription;
    private String acquirerTxnId;
    private String errorMessage;
    private Integer attemptCount;
    private Integer maxAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextRetryAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;

    private List<AttemptSummary> attempts;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AttemptSummary {
        private Integer attemptNumber;
        private String status;
        private String errorCode;
        private String errorMessage;
        private Long durationMs;
        private String circuitBreakerState;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime attemptedAt;
    }
}
