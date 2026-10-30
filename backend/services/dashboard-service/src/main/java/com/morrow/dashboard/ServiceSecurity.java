package com.morrow.dashboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ServiceSecurity {
    @Bean
    @Profile("!secured")
    SecurityFilterChain localApi(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(requests -> requests.anyRequest().permitAll()).build();
    }

    @Bean
    @Profile("secured")
    SecurityFilterChain securedApi(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(requests -> requests
                .requestMatchers("/actuator/health/**").permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(resource -> resource.jwt()).build();
    }
}
