package ru.mtuci.demo.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseTypeCreationRequest {
    private Long duration;
    private String description;
    private String name;
}
