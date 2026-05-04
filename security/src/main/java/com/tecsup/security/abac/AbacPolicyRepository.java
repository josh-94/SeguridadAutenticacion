package com.tecsup.security.abac;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbacPolicyRepository extends JpaRepository<AbacPolicy, Long> {
    List<AbacPolicy> findByResourceAndActionAndActiveTrue(String resource, String action);
}
