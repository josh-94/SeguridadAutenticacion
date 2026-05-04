package com.tecsup.security.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String totpCode;
}
