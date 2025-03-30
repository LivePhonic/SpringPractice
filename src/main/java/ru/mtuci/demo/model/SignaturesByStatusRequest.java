package ru.mtuci.demo.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignaturesByStatusRequest {
    private SignatureStatus status;
}
