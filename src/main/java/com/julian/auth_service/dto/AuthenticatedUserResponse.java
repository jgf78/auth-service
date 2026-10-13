package com.julian.auth_service.dto;

public record AuthenticatedUserResponse(
        String subject,
        String username,
        String email,
        String issuer
) {
}
