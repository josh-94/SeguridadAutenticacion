package com.tecsup.security.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<AuditLog> findByDecisionOrderByCreatedAtDesc(String decision);
}
