package com.drystorm.api.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutPaymentRequest {

    @NotBlank(message = "idempotencyKey é obrigatório")
    @Size(min = 8, max = 64, message = "idempotencyKey deve ter entre 8 e 64 caracteres")
    private String idempotencyKey;

    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "0.01", message = "amount deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "amount inválido")
    private BigDecimal amount;

    @Size(max = 3)
    private String currency = "BRL";

    @NotNull(message = "paymentMethod é obrigatório")
    private PaymentMethod paymentMethod;

    @Valid
    private CardDetails card;

    @Valid
    private PixDetails pix;

    @Size(max = 255)
    private String description;

    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PIX
    }

    @Data
    public static class CardDetails {
        @NotBlank(message = "card.holder é obrigatório")
        @Size(max = 100)
        private String holder;

        @NotBlank(message = "card.number é obrigatório")
        @Pattern(regexp = "\\d{13,19}", message = "Número do cartão inválido")
        private String number;

        @NotBlank(message = "card.expiry é obrigatório")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Validade no formato MM/AA")
        private String expiry;

        @NotBlank(message = "card.cvv é obrigatório")
        @Pattern(regexp = "\\d{3,4}", message = "CVV inválido")
        private String cvv;

        @Size(max = 20)
        private String brand;
    }

    @Data
    public static class PixDetails {
        @NotBlank(message = "pix.key é obrigatório")
        @Size(max = 100)
        private String key;
    }
}
