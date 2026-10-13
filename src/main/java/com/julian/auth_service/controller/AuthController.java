package com.julian.auth_service.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.julian.auth_service.dto.AuthenticatedUserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthenticatedUserResponse getAuthenticatedUser(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No authenticated user"
            );
        }

        return new AuthenticatedUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                Objects.toString(jwt.getIssuer(), null)
        );
    }

    @GetMapping("/admin")
    public Map<String, String> getAdminMessage() {
        return Map.of(
                "message",
                "Acceso autorizado para administradores"
        );
    }
}