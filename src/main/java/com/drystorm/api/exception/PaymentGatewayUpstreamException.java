package com.drystorm.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class PaymentGatewayUpstreamException extends RuntimeException {

    private final HttpStatusCode status;

    public PaymentGatewayUpstreamException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }
}
