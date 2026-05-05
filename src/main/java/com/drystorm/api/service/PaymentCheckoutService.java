package com.drystorm.api.service;

import com.drystorm.api.config.PaymentGatewayProperties;
import com.drystorm.api.dto.integration.GatewayErrorBody;
import com.drystorm.api.dto.integration.GatewayPaymentPayload;
import com.drystorm.api.dto.request.CheckoutPaymentRequest;
import com.drystorm.api.dto.response.PaymentGatewayPaymentResponse;
import com.drystorm.api.exception.BusinessException;
import com.drystorm.api.exception.PaymentGatewayUpstreamException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCheckoutService {

    private final RestClient paymentGatewayRestClient;
    private final PaymentGatewayProperties properties;
    private final ObjectMapper objectMapper;

    public PaymentGatewayPaymentResponse create(CheckoutPaymentRequest req) {
        validateMethodPayload(req);
        if (!properties.isEnabled()) {
            throw new BusinessException("Gateway de pagamentos desabilitado.");
        }
        GatewayPaymentPayload payload = toPayload(req);
        try {
            return paymentGatewayRestClient.post()
                    .uri("/api/v1/payments")
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw mapError(response);
                    })
                    .body(PaymentGatewayPaymentResponse.class);
        } catch (PaymentGatewayUpstreamException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Falha de rede ao chamar payment-gateway: {}", e.getMessage());
            throw new BusinessException("Gateway de pagamentos indisponível. Tente novamente em instantes.");
        }
    }

    public PaymentGatewayPaymentResponse getById(UUID id) {
        if (!properties.isEnabled()) {
            throw new BusinessException("Gateway de pagamentos desabilitado.");
        }
        try {
            return paymentGatewayRestClient.get()
                    .uri("/api/v1/payments/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw mapError(response);
                    })
                    .body(PaymentGatewayPaymentResponse.class);
        } catch (PaymentGatewayUpstreamException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Falha de rede ao consultar payment-gateway: {}", e.getMessage());
            throw new BusinessException("Gateway de pagamentos indisponível. Tente novamente em instantes.");
        }
    }

    private void validateMethodPayload(CheckoutPaymentRequest req) {
        switch (req.getPaymentMethod()) {
            case CREDIT_CARD, DEBIT_CARD -> {
                if (req.getCard() == null) {
                    throw new BusinessException("Dados do cartão são obrigatórios para este método de pagamento.");
                }
            }
            case PIX -> {
                if (req.getPix() == null) {
                    throw new BusinessException("Chave PIX é obrigatória para pagamento PIX.");
                }
            }
        }
    }

    private GatewayPaymentPayload toPayload(CheckoutPaymentRequest req) {
        GatewayPaymentPayload.GatewayPaymentPayloadBuilder b = GatewayPaymentPayload.builder()
                .idempotencyKey(req.getIdempotencyKey())
                .merchantId(properties.getMerchantId())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .paymentMethod(req.getPaymentMethod().name())
                .description(req.getDescription());
        if (req.getCard() != null) {
            CheckoutPaymentRequest.CardDetails c = req.getCard();
            b.card(GatewayPaymentPayload.CardPayload.builder()
                    .holder(c.getHolder())
                    .number(c.getNumber())
                    .expiry(c.getExpiry())
                    .cvv(c.getCvv())
                    .brand(c.getBrand())
                    .build());
        }
        if (req.getPix() != null) {
            b.pix(GatewayPaymentPayload.PixPayload.builder()
                    .key(req.getPix().getKey())
                    .build());
        }
        return b.build();
    }

    private PaymentGatewayUpstreamException mapError(ClientHttpResponse response) {
        HttpStatusCode status;
        try {
            status = response.getStatusCode();
        } catch (IOException e) {
            return new PaymentGatewayUpstreamException(HttpStatus.BAD_GATEWAY,
                    "Erro ao comunicar com o gateway de pagamentos.");
        }
        String message = "Erro no gateway de pagamentos.";
        try {
            String raw = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            if (StringUtils.hasText(raw)) {
                GatewayErrorBody err = objectMapper.readValue(raw, GatewayErrorBody.class);
                if (StringUtils.hasText(err.getMessage())) {
                    message = err.getMessage();
                }
            }
        } catch (IOException e) {
            log.debug("Corpo de erro do gateway ilegível: {}", e.getMessage());
        }
        return new PaymentGatewayUpstreamException(status, message);
    }
}
