package com.tecsup.security.abac;

import lombok.Builder;
import lombok.Data;

/**
 * Contexto de evaluacion ABAC.
 * Contiene todos los atributos necesarios para evaluar una politica.
 */
@Data
@Builder
public class AbacContext {
    // Atributos del sujeto (usuario)
    private Long userId;
    private String username;
    private String userArea;
    private String userRegion;
    private String userSeniority;
    private String userDepartment;

    // Atributos del recurso
    private String resourceType;   // products
    private Long   resourceId;
    private String resourceRegion;
    private Long   resourceOwnerId;
    private String resourceStatus;

    // Accion solicitada
    private String action;         // read, create, update, delete

    // Contexto de entorno
    private int    hourOfDay;      // 0-23
    private String ipAddress;
}
