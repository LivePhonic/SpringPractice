package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationSignatureAudit;

public interface SignatureAuditRepository extends JpaRepository<ApplicationSignatureAudit, Long> {
}
