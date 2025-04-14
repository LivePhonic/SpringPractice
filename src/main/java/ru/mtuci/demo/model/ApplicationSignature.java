package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "signatures")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationSignature {
    @Id
    private UUID id;

    private String threatName;

    private String firstBytes;

    private String remainderHash;

    private Integer remainderLength;

    private String fileType;

    private Integer offsetStart;

    private Integer offsetEnd;

    @Column(length = 2048)
    private String digitalSignature;

    private Date updatedAt;

    @Enumerated(EnumType.STRING)
    private SignatureStatus status;
}
