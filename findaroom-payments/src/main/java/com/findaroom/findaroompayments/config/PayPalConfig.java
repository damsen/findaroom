package com.findaroom.findaroompayments.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String payPalClientId;

    @Value("${paypal.secret}")
    private String payPalSecret;

    @Bean
    public PayPalEnvironment payPalEnvironment() {
        return new PayPalEnvironment.Sandbox(this.payPalClientId, this.payPalSecret);
    }

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        return new PayPalHttpClient(payPalEnvironment());
    }
}
