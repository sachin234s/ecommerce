package com.morrow.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GatewaySecurity {
    @Bean
    @Profile("!secured")
    SecurityWebFilterChain localGateway(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll()).build();
    }

    @Bean
    @Profile("secured")
    SecurityWebFilterChain securedGateway(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.pathMatchers("/actuator/health/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(resource -> resource.jwt()).build();
    }
}
