package com.tecsup.security.product.service;

import com.tecsup.security.abac.*;
import com.tecsup.security.audit.AuditService;
import com.tecsup.security.product.model.Product;
import com.tecsup.security.product.repository.ProductRepository;
import com.tecsup.security.rbac.model.User;
import com.tecsup.security.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AbacPolicyEngine policyEngine;
    private final AuditService auditService;

    public List<Product> findAll(String username, String ip) {
        User user = getUser(username);
        // Para el listado general usamos la region del usuario como resourceRegion
        // para que la politica SAME_REGION pueda evaluarse correctamente
        AbacContext ctx = AbacContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userArea(user.getArea())
                .userRegion(user.getRegion())
                .userSeniority(user.getSeniority())
                .userDepartment(user.getDepartment())
                .resourceType("products")
                .resourceRegion(user.getRegion())
                .action("read")
                .hourOfDay(LocalTime.now().getHour())
                .ipAddress(ip)
                .build();
        AbacPolicyEngine.AbacDecision decision = policyEngine.evaluate(ctx);
        auditService.log(user.getId(), username, "products.read", "products", null,
                decision.allowed() ? "ALLOW" : "DENY", decision.reason(), ip);
        if (!decision.allowed()) throw new AccessDeniedException(decision.reason());
        return productRepository.findAll();
    }

    public Product findById(Long id, String username, String ip) {
        Product product = getProduct(id);
        User user = getUser(username);
        AbacContext ctx = buildContext(user, product, "read", ip);
        AbacPolicyEngine.AbacDecision decision = policyEngine.evaluate(ctx);
        auditService.log(user.getId(), username, "products.read", "products", id.toString(),
                decision.allowed() ? "ALLOW" : "DENY", decision.reason(), ip);
        if (!decision.allowed()) throw new AccessDeniedException(decision.reason());
        return product;
    }

    @Transactional
    public Product create(Product product, String username, String ip) {
        User user = getUser(username);
        product.setOwner(user);
        AbacContext ctx = buildContext(user, product, "create", ip);
        AbacPolicyEngine.AbacDecision decision = policyEngine.evaluate(ctx);
        auditService.log(user.getId(), username, "products.create", "products", null,
                decision.allowed() ? "ALLOW" : "DENY", decision.reason(), ip);
        if (!decision.allowed()) throw new AccessDeniedException(decision.reason());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, Product updated, String username, String ip) {
        Product product = getProduct(id);
        User user = getUser(username);
        AbacContext ctx = buildContext(user, product, "update", ip);
        AbacPolicyEngine.AbacDecision decision = policyEngine.evaluate(ctx);
        auditService.log(user.getId(), username, "products.update", "products", id.toString(),
                decision.allowed() ? "ALLOW" : "DENY", decision.reason(), ip);
        if (!decision.allowed()) throw new AccessDeniedException(decision.reason());

        product.setName(updated.getName());
        product.setCategory(updated.getCategory());
        product.setRegion(updated.getRegion());
        product.setStatus(updated.getStatus());
        product.setPrice(updated.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id, String username, String ip) {
        Product product = getProduct(id);
        User user = getUser(username);
        AbacContext ctx = buildContext(user, product, "delete", ip);
        AbacPolicyEngine.AbacDecision decision = policyEngine.evaluate(ctx);
        auditService.log(user.getId(), username, "products.delete", "products", id.toString(),
                decision.allowed() ? "ALLOW" : "DENY", decision.reason(), ip);
        if (!decision.allowed()) throw new AccessDeniedException(decision.reason());
        productRepository.deleteById(id);
    }

    // ----------------------------------------------------------------
    private AbacContext buildContext(User user, Product product, String action, String ip) {
        return AbacContext.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userArea(user.getArea())
                .userRegion(user.getRegion())
                .userSeniority(user.getSeniority())
                .userDepartment(user.getDepartment())
                .resourceType("products")
                .resourceId(product != null ? product.getId() : null)
                .resourceRegion(product != null ? product.getRegion() : null)
                .resourceOwnerId(product != null && product.getOwner() != null ? product.getOwner().getId() : null)
                .resourceStatus(product != null ? product.getStatus() : null)
                .action(action)
                .hourOfDay(LocalTime.now().getHour())
                .ipAddress(ip)
                .build();
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }
}
