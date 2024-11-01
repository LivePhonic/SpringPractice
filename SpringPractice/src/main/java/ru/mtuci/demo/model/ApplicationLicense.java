package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationLicense {
    @Id
    @GeneratedValue
    private UUID id;

    private String key;
    private LocalDateTime activationDate;
    private LocalDateTime expirationDate;
    private String deviceId;
    private boolean isBlocked;

}
