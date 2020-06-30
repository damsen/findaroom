package com.findaroom.findaroomgateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.security.oauth2.gateway.TokenRelayGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, TokenRelayGatewayFilterFactory filterFactory) {
        return builder.routes()
                .route("core-public-api", r -> r
                        .path("/api/v1/public/**")
                        .uri("http://localhost:8080"))
                .route("core-secured-api", r -> r
                        .path("/api/v1/user-ops/**").or()
                        .path("/api/v1/host-ops/**")
                        .filters(f -> f.filter(filterFactory.apply()))
                        .uri("http://localhost:8080"))
                .route("payments", r -> r
                        .path("/api/v1/payments/**")
                        .filters(f -> f.filter(filterFactory.apply()))
                        .uri("http://localhost:8081"))
                .route("notifications", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f.filter(filterFactory.apply()))
                        .uri("http://localhost:8082"))
                .route("users", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.filter(filterFactory.apply()))
                        .uri("http://localhost:8083"))
                .build();
    }
}
