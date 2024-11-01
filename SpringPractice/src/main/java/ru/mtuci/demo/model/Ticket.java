package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ticket {
    private String deviceId;
    private long lifetime;
    private LocalDateTime activationDate;
    private LocalDateTime expirationDate;
    private boolean licenseBlocked;
    private String digitalSignature;
}
