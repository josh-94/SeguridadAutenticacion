package com.tecsup.security.auth;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private boolean mfaRequired;
    private String message;
    // Si mfaRequired=true, el token es temporal (pre-auth)
    // Si mfaRequired=false, el token es definitivo
}
