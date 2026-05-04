package com.tecsup.security.auth;

import com.tecsup.security.audit.AuditService;
import com.tecsup.security.config.AppUserDetailsService;
import com.tecsup.security.config.JwtService;
import com.tecsup.security.rbac.model.User;
import com.tecsup.security.rbac.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AppUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------
    // PASO 1: Validar credenciales
    // ----------------------------------------------------------------
    public AuthResponse login(LoginRequest request, String ip) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            auditService.log(null, request.getUsername(), "LOGIN", null, null, "DENY", "Credenciales invalidas", ip);
            throw new BadCredentialsException("Usuario o contrasena incorrectos");
        }

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        // Si tiene MFA activo, emitir token provisional (mfaPending=true)
        if (user.isMfaEnabled()) {
            UserDetails ud = userDetailsService.loadUserByUsername(user.getUsername());
            String preToken = jwtService.generateToken(ud, Map.of("mfaPending", true));
            auditService.log(user.getId(), user.getUsername(), "LOGIN_STEP1", null, null, "ALLOW", "Credenciales OK, MFA pendiente", ip);
            return AuthResponse.builder()
                    .token(preToken)
                    .mfaRequired(true)
                    .message("Credenciales validas. Ingrese codigo MFA.")
                    .build();
        }

        // Sin MFA: emitir token definitivo
        UserDetails ud = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(ud, Map.of("mfaPending", false));
        auditService.log(user.getId(), user.getUsername(), "LOGIN", null, null, "ALLOW", "Login exitoso sin MFA", ip);
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .message("Login exitoso")
                .build();
    }

    // ----------------------------------------------------------------
    // PASO 2: Verificar codigo TOTP
    // ----------------------------------------------------------------
    public AuthResponse verifyMfa(MfaVerifyRequest request, String ip) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            throw new RuntimeException("MFA no configurado para este usuario");
        }

        boolean valid = verifyTotp(user.getMfaSecret(), request.getTotpCode());
        if (!valid) {
            auditService.log(user.getId(), user.getUsername(), "MFA_VERIFY", null, null, "DENY", "Codigo MFA invalido", ip);
            throw new BadCredentialsException("Codigo MFA invalido o expirado");
        }

        UserDetails ud = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(ud, Map.of("mfaPending", false));
        auditService.log(user.getId(), user.getUsername(), "MFA_VERIFY", null, null, "ALLOW", "MFA verificado correctamente", ip);
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .message("Autenticacion completa")
                .build();
    }

    // ----------------------------------------------------------------
    // Generar secreto TOTP y QR data para configurar app autenticadora
    // ----------------------------------------------------------------
    public MfaSetupResponse setupMfa(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userRepository.save(user);
        String otpAuthUrl = "otpauth://totp/TecsupSecurity:" + username + "?secret=" + secret + "&issuer=TecsupSecurity";
        return new MfaSetupResponse(secret, otpAuthUrl);
    }

    // ----------------------------------------------------------------
    // Verificar TOTP con la libreria samstevens
    // ----------------------------------------------------------------
    private boolean verifyTotp(String secret, String code) {
        try {
            String cleanCode = code.trim().replaceAll("\\s+", "");
            DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
            DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
            verifier.setAllowedTimePeriodDiscrepancy(2); // tolera ±2 periodos de 30s = ±60s de desfase de reloj
            return verifier.isValidCode(secret, cleanCode);
        } catch (Exception e) {
            return false;
        }
    }

    public record MfaSetupResponse(String secret, String otpAuthUrl) {}
}
