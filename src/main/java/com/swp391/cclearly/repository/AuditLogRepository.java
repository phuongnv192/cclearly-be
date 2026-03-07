package com.swp391.cclearly.repository;

import com.swp391.cclearly.entity.AuditLog;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
  Page<AuditLog> findAllByOrderByLogIdDesc(Pageable pageable);
  Page<AuditLog> findByActionContainingIgnoreCaseOrderByLogIdDesc(String action, Pageable pageable);
}
