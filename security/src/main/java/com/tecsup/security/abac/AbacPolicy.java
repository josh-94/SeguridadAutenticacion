package com.tecsup.security.abac;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "abac_policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbacPolicy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 10)
    private String effect;  // ALLOW | DENY

    @Column(name = "condition_expression", nullable = false, columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(nullable = false)
    private int priority = 10;

    @Column(nullable = false)
    private boolean active = true;
}
