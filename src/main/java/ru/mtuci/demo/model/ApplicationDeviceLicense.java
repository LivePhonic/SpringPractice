package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "device_license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDeviceLicense {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "licenseId")
    private ApplicationLicense license;

    @ManyToOne
    @JoinColumn(name = "deviceId")
    private ApplicationDevice device;

    private Date activationDate;
}
