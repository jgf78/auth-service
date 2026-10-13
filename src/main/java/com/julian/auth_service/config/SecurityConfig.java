package com.julian.auth_service.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos de Actuator
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()

                // Swagger UI y documentación OpenAPI
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // Endpoints de autenticación
                .requestMatchers("/api/auth/admin").hasRole("ADMIN")
                .requestMatchers("/api/auth/me").authenticated()

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Dominios web autorizados, configurados mediante variable de entorno.
        String allowedOrigins = System.getenv().getOrDefault(
            "CORS_ALLOWED_ORIGINS",
            "http://localhost:3000,http://localhost:4200"
        );

        configuration.setAllowedOrigins(
            List.of(allowedOrigins.split(",")).stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList()
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type", "Accept", "Origin")
        );

        configuration.setExposedHeaders(
            List.of("WWW-Authenticate")
        );

        // La API utiliza Bearer JWT, no autenticación mediante cookies.
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Roles generales del realm
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            Object realmRolesClaim = realmAccess == null
                    ? null
                    : realmAccess.get("roles");

            if (realmRolesClaim instanceof Collection<?> realmRoles) {
                realmRoles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }

            // Roles específicos de los clientes
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null) {
                resourceAccess.forEach((clientId, clientAccess) -> {
                    if (clientAccess instanceof Map<?, ?> clientAccessMap) {
                        Object clientRolesClaim = clientAccessMap.get("roles");

                        if (clientRolesClaim instanceof Collection<?> clientRoles) {
                            clientRoles.stream()
                                    .filter(String.class::isInstance)
                                    .map(String.class::cast)
                                    .map(role -> new SimpleGrantedAuthority(
                                            "ROLE_" + clientId.toUpperCase()
                                                    + "_" + role
                                    ))
                                    .forEach(authorities::add);
                        }
                    }
                });
            }

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}