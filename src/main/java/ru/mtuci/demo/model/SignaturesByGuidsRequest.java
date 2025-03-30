package ru.mtuci.demo.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignaturesByGuidsRequest {
    private List<UUID> guids;
}