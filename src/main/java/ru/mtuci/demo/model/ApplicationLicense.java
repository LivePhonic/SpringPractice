package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

//TODO: 1. Связь с owner должна быть такая же, как с user
//TODO: 2. int может быть маловат для длительности

@Entity
@Table(name = "license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationLicense {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    @ManyToOne
    @JoinColumn(name = "productId")
    private ApplicationProduct product;

    @ManyToOne
    @JoinColumn(name = "typeId")
    private ApplicationLicenseType licenseType;

    private Date firstActivationDate;

    private Date endingDate;

    private boolean blocked;

    private int deviceCount;

    private Long ownerId;

    private int duration;

    private String description;
}

