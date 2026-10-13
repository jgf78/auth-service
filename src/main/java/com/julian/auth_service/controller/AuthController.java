package com.julian.auth_service.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private static final Set<String> PLATFORM_ROLES =
            Set.of("USER", "ADMIN");

    @GetMapping("/me")
    public AuthenticatedUserResponse getAuthenticatedUser(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No authenticated user"
            );
        }

        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        Object rolesClaim = realmAccess != null
                ? realmAccess.get("roles")
                : null;

        List<String> roles = rolesClaim instanceof Collection<?> claimRoles
                ? claimRoles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .filter(PLATFORM_ROLES::contains)
                        .sorted()
                        .toList()
                : List.of();

        return new AuthenticatedUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                Objects.toString(jwt.getIssuer(), null),
                roles
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