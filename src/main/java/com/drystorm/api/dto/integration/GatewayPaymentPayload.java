package com.drystorm.api.dto.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Corpo enviado ao payment-gateway (espelha o contrato de {@code PaymentRequest} do gateway).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayPaymentPayload {

    private String idempotencyKey;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private CardPayload card;
    private PixPayload pix;
    private String description;

    @Data
    @Builder
    public static class CardPayload {
        private String holder;
        private String number;
        private String expiry;
        private String cvv;
        private String brand;
    }

    @Data
    @Builder
    public static class PixPayload {
        private String key;
    }
}
