package com.drystorm.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.payment-gateway")
public class PaymentGatewayProperties {

    /**
     * URL base do payment-gateway (ex.: http://localhost:8081).
     */
    private String baseUrl = "http://localhost:8081";

    /**
     * Identificador do merchant enviado ao gateway (não exposto ao cliente).
     */
    private String merchantId = "drystorm-001";

    private boolean enabled = true;

    /**
     * Opcional: repasse para o gateway se você proteger o serviço com header (ex.: API key).
     */
    private String apiKey = "";
}
