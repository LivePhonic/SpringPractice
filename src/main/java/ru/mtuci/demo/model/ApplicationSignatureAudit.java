package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "signatures_audit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationSignatureAudit {
    @Id
    @GeneratedValue
    private Long auditId;

    private UUID signatureId;

    private Long changedBy;

    private String changedType;

    private Date changedAt;

    private String fieldChanged;
}
