package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "license_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationLicenseHistory {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "licenseId")
    private ApplicationLicense license;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    private String status;

    private Date changeDate;

    private String description;
}

