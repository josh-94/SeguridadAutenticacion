package com.tecsup.security.rbac.service;

import com.tecsup.security.rbac.dto.*;
import com.tecsup.security.rbac.model.*;
import com.tecsup.security.rbac.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    public UserDto findById(Long id) {
        return UserDto.from(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id)));
    }

    @Transactional
    public UserDto create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("El username ya existe");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("El email ya existe");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .area(req.getArea())
                .region(req.getRegion())
                .seniority(req.getSeniority())
                .department(req.getDepartment())
                .enabled(true)
                .build();
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, CreateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        user.setEmail(req.getEmail());
        user.setArea(req.getArea());
        user.setRegion(req.getRegion());
        user.setSeniority(req.getSeniority());
        user.setDepartment(req.getDepartment());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("Usuario no encontrado: " + id);
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        user.getRoles().add(role);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        return UserDto.from(userRepository.save(user));
    }
}
