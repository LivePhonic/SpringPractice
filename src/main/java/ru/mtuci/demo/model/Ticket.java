package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ticket {
    private int deviceCount;
    private long lifetime;
    private Date activationDate;
    private Date expirationDate;
    private boolean licenseBlocked;
    private String digitalSignature;
}
