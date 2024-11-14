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
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private ApplicationLicense license;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    private String status;

    private Date change_date;

    private String description;
}

