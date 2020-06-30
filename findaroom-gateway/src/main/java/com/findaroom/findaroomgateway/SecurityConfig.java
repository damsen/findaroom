package com.findaroom.findaroomgateway;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/api/v1/public/**").permitAll()
                .pathMatchers("/api/v1/user-ops/**").authenticated()
                .pathMatchers("/api/v1/host-ops/**").authenticated()
                .pathMatchers("/api/v1/payments/**").authenticated()
                .pathMatchers("/api/v1/notifications/**").authenticated()
                .pathMatchers("/api/v1/users/**").authenticated()
                .and()
                .oauth2Login()
                .and()
                .oauth2ResourceServer().jwt()
                .and().and().build();
    }
}
