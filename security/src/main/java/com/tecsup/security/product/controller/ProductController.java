package com.tecsup.security.product.controller;

import com.tecsup.security.product.model.Product;
import com.tecsup.security.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> findAll(@AuthenticationPrincipal UserDetails user,
                                                  HttpServletRequest req) {
        return ResponseEntity.ok(productService.findAll(user.getUsername(), req.getRemoteAddr()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails user,
                                             HttpServletRequest req) {
        return ResponseEntity.ok(productService.findById(id, user.getUsername(), req.getRemoteAddr()));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product,
                                           @AuthenticationPrincipal UserDetails user,
                                           HttpServletRequest req) {
        return ResponseEntity.ok(productService.create(product, user.getUsername(), req.getRemoteAddr()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                           @RequestBody Product product,
                                           @AuthenticationPrincipal UserDetails user,
                                           HttpServletRequest req) {
        return ResponseEntity.ok(productService.update(id, product, user.getUsername(), req.getRemoteAddr()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user,
                                        HttpServletRequest req) {
        productService.delete(id, user.getUsername(), req.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
