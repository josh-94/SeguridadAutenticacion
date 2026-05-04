package com.tecsup.security.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId, String username, String action,
                    String resource, String resourceId,
                    String decision, String reason, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .decision(decision)
                .reason(reason)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> findByUsername(String username) {
        return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username);
    }
}
