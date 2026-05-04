package com.tecsup.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(request, http.getRemoteAddr()));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request,
                                                   HttpServletRequest http) {
        return ResponseEntity.ok(authService.verifyMfa(request, http.getRemoteAddr()));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa(@AuthenticationPrincipal UserDetails userDetails) {
        AuthService.MfaSetupResponse setup = authService.setupMfa(userDetails.getUsername());
        return ResponseEntity.ok(setup);
    }
}
