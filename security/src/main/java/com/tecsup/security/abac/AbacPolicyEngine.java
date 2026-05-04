package com.tecsup.security.abac;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Policy Engine ABAC.
 *
 * Logica de evaluacion:
 * 1. Carga politicas activas para resource+action, ordenadas por prioridad (menor numero = mayor prioridad).
 * 2. Evalua cada politica. La primera que coincide define el efecto final (ALLOW/DENY).
 * 3. Si ninguna coincide, deniega por defecto (deny-by-default).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AbacPolicyEngine {

    private final AbacPolicyRepository policyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AbacDecision evaluate(AbacContext ctx) {
        List<AbacPolicy> policies = policyRepository
                .findByResourceAndActionAndActiveTrue(ctx.getResourceType(), ctx.getAction())
                .stream()
                .sorted(Comparator.comparingInt(AbacPolicy::getPriority))
                .toList();

        for (AbacPolicy policy : policies) {
            if (matches(policy, ctx)) {
                log.debug("ABAC: policy='{}' effect='{}' for user='{}' action='{}' resource='{}'",
                        policy.getName(), policy.getEffect(), ctx.getUsername(), ctx.getAction(), ctx.getResourceType());
                boolean allowed = "ALLOW".equals(policy.getEffect());
                return new AbacDecision(allowed,
                        policy.getEffect() + " por politica: " + policy.getName());
            }
        }

        // Deny by default
        return new AbacDecision(false, "Sin politica aplicable: acceso denegado por defecto");
    }

    private boolean matches(AbacPolicy policy, AbacContext ctx) {
        try {
            JsonNode cond = objectMapper.readTree(policy.getConditionExpression());
            String type = cond.path("type").asText();

            return switch (type) {
                case "SAME_REGION" ->
                        ctx.getUserRegion() != null && ctx.getUserRegion().equalsIgnoreCase(ctx.getResourceRegion());

                case "USER_AREA" -> {
                    String requiredArea = cond.path("value").asText();
                    yield requiredArea.equalsIgnoreCase(ctx.getUserArea());
                }

                case "OWNER_OR_SENIORITY" -> {
                    String requiredSeniority = cond.path("seniority").asText("senior");
                    boolean isOwner = ctx.getUserId() != null && ctx.getUserId().equals(ctx.getResourceOwnerId());
                    boolean hasSeniority = requiredSeniority.equalsIgnoreCase(ctx.getUserSeniority());
                    yield isOwner || hasSeniority;
                }

                case "OUTSIDE_HOURS" -> {
                    int start = cond.path("start").asInt(8);
                    int end   = cond.path("end").asInt(18);
                    // Aplica la regla DENY si esta FUERA del horario
                    yield ctx.getHourOfDay() < start || ctx.getHourOfDay() >= end;
                }

                default -> {
                    log.warn("ABAC: tipo de condicion desconocido '{}'", type);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("ABAC: error evaluando politica '{}': {}", policy.getName(), e.getMessage());
            return false;
        }
    }

    public record AbacDecision(boolean allowed, String reason) {}
}
