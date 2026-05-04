package com.tecsup.security.rbac.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"resource","action"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String description;
}
