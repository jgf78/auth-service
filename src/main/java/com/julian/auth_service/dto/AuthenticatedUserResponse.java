package com.julian.auth_service.dto;

import java.util.List;

public record AuthenticatedUserResponse(
        String subject,
        String username,
        String email,
        String issuer,
        List<String> roles
) {
}