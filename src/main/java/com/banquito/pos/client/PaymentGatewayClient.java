package com.banquito.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.banquito.pos.client.dto.TransaccionRequestDTO;
import com.banquito.pos.client.dto.TransaccionResponseDTO;

@FeignClient(name = "payment-gateway", url = "${payment-gateway.url}")
public interface PaymentGatewayClient {

    @PostMapping("/v1/transacciones")
    TransaccionResponseDTO procesarTransaccion(
            @RequestHeader("X-Request-ID") String requestId,
            @RequestBody TransaccionRequestDTO transaccion);
} 