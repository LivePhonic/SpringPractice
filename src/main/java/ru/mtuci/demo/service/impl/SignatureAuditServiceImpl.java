package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignatureAudit;
import ru.mtuci.demo.repository.SignatureAuditRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
public class SignatureAuditServiceImpl {
    private final SignatureAuditRepository signatureAuditRepository;

    public SignatureAuditServiceImpl(SignatureAuditRepository signatureAuditRepository) {
        this.signatureAuditRepository = signatureAuditRepository;
    }

    public void createSignatureAudit(UUID signatureId, Long userId, String changedType, Date changedAt, String fieldChanged){
        ApplicationSignatureAudit applicationSignatureAudit = new ApplicationSignatureAudit();
        applicationSignatureAudit.setSignatureId(signatureId);
        applicationSignatureAudit.setChangedAt(changedAt);
        applicationSignatureAudit.setChangedType(changedType);
        applicationSignatureAudit.setFieldChanged(fieldChanged);
        applicationSignatureAudit.setChangedBy(userId);

        signatureAuditRepository.save(applicationSignatureAudit);
    }

    public List<ApplicationSignatureAudit> getAllAuditRecords() {
        return signatureAuditRepository.findAll();
    }
}
