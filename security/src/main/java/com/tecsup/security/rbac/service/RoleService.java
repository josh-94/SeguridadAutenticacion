package com.tecsup.security.rbac.service;

import com.tecsup.security.rbac.dto.CreateRoleRequest;
import com.tecsup.security.rbac.model.*;
import com.tecsup.security.rbac.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));
    }

    @Transactional
    public Role create(CreateRoleRequest req) {
        if (roleRepository.existsByName(req.getName()))
            throw new RuntimeException("El rol ya existe");
        Role role = Role.builder().name(req.getName().toUpperCase()).description(req.getDescription()).build();
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, CreateRoleRequest req) {
        Role role = findById(id);
        role.setName(req.getName().toUpperCase());
        role.setDescription(req.getDescription());
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    @Transactional
    public Role assignPermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        Permission perm = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        role.getPermissions().add(perm);
        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermission(Long roleId, Long permissionId) {
        Role role = findById(roleId);
        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        return roleRepository.save(role);
    }
}
