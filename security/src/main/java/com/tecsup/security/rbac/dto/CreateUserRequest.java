package com.tecsup.security.rbac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank private String username;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    private String area;
    private String region;
    private String seniority;
    private String department;
}
