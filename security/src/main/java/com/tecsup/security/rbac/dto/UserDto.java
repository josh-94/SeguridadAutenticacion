package com.tecsup.security.rbac.dto;

import com.tecsup.security.rbac.model.User;
import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String area;
    private String region;
    private String seniority;
    private String department;
    private boolean mfaEnabled;
    private boolean enabled;
    private List<String> roles;

    public static UserDto from(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setArea(u.getArea());
        dto.setRegion(u.getRegion());
        dto.setSeniority(u.getSeniority());
        dto.setDepartment(u.getDepartment());
        dto.setMfaEnabled(u.isMfaEnabled());
        dto.setEnabled(u.isEnabled());
        dto.setRoles(u.getRoles().stream().map(r -> r.getName()).toList());
        return dto;
    }
}
