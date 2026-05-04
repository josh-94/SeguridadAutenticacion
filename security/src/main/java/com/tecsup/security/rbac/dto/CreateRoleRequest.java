package com.tecsup.security.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank private String name;
    private String description;
}
