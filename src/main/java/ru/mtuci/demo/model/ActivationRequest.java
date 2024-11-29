package ru.mtuci.demo.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationRequest {
    private String activationCode;
    private String name;
    private String mac_address;
}
