package ru.mtuci.demo.model;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignaturesUpdateRequest {
    private UUID signatureId;
    private String threatName;
    private String firstBytes;
    private Integer remainderLength;
    private String fileType;
    private Integer offsetStart;
    private Integer offsetEnd;
    private SignatureStatus status;
}