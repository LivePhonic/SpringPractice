package ru.mtuci.demo.model;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignaturesDeleteRequest {
    private UUID signatureUUID;
}
