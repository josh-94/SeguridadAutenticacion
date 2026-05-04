package com.tecsup.security.rbac.controller;

import com.tecsup.security.rbac.dto.CreateRoleRequest;
import com.tecsup.security.rbac.model.Role;
import com.tecsup.security.rbac.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('roles.read')")
    public ResponseEntity<List<Role>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.read')")
    public ResponseEntity<Role> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles.create')")
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleRequest req) {
        return ResponseEntity.ok(roleService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.update')")
    public ResponseEntity<Role> update(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest req) {
        return ResponseEntity.ok(roleService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('roles.update')")
    public ResponseEntity<Role> assignPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.assignPermission(roleId, permissionId));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('roles.update')")
    public ResponseEntity<Role> removePermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermission(roleId, permissionId));
    }
}
