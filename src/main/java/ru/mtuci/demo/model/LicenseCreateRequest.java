package ru.mtuci.demo.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseCreateRequest {
    private Long productId;
    private Long ownerId;
    private Long licenseTypeId;
    private Long count;
}
