package com.drystorm.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(PaymentGatewayProperties.class)
public class PaymentGatewayClientConfig {

    @Bean
    public RestClient paymentGatewayRestClient(RestClient.Builder builder, PaymentGatewayProperties props) {
        RestClient.Builder b = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (StringUtils.hasText(props.getApiKey())) {
            b = b.defaultHeader("X-Api-Key", props.getApiKey());
        }
        return b.build();
    }
}
