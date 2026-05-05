package com.drystorm.api.controller;

import com.drystorm.api.dto.request.CheckoutPaymentRequest;
import com.drystorm.api.dto.response.ApiResponse;
import com.drystorm.api.dto.response.PaymentGatewayPaymentResponse;
import com.drystorm.api.service.PaymentCheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout/payments")
@RequiredArgsConstructor
@Tag(name = "Checkout / Pagamentos", description = "Proxy para o payment-gateway DryStorm (merchant e URL configurados no servidor)")
public class CheckoutPaymentController {

    private final PaymentCheckoutService paymentCheckoutService;

    @PostMapping
    @Operation(summary = "Iniciar pagamento",
               description = "Encaminha a solicitação ao microserviço payment-gateway. "
                             + "O merchantId é definido pela API; não envie dados de cartão diretamente ao gateway a partir do front sem HTTPS.")
    public ResponseEntity<ApiResponse<PaymentGatewayPaymentResponse>> create(
            @Valid @RequestBody CheckoutPaymentRequest request) {

        PaymentGatewayPaymentResponse data = paymentCheckoutService.create(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar status do pagamento")
    public ResponseEntity<ApiResponse<PaymentGatewayPaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentCheckoutService.getById(id)));
    }
}
