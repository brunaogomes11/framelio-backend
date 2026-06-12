package com.gomes.photographer_manager.config.mercadopago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfig {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Bean
    public MercadoPagoConfig initMP() {
        com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);
        return this;
    }
}
