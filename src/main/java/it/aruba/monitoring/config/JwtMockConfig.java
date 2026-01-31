package it.aruba.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

@Configuration
public class JwtMockConfig {

    @Bean
    JwtDecoder jwtDecoder() {


        return token -> {

            if (token == null || token.isBlank()) {
                throw new JwtException("Token cannot be empty");
            }

            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "mock-user")
                    .claim("scope", "api")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        };
    }
}
