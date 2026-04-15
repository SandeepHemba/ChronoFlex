package com.example.ChronoFlex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleSuccessHandler googleSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(GoogleSuccessHandler googleSuccessHandler) {
        this.googleSuccessHandler = googleSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   // ✅ new style

//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/admin/**",
//                                "/student/**",
//                                "/oauth2/**",
//                                "/login/**",
//                                "/",
//                                "/verify",
//                                "/home",
//                                "/forms"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )

                .oauth2Login(oauth -> oauth
                        .successHandler(googleSuccessHandler)
                );

        return http.build();
    }
}
