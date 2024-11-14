package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

import java.util.Date;


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

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ApplicationProduct product;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private ApplicationLicenseType license_type;

    private Date first_activation_date;

    private Date ending_date;

    private boolean blocked;

    private int device_count;

    private UUID owner_id;

    private int duration;

    private String description;
}

