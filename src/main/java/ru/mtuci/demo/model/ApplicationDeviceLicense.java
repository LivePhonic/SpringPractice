package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "device_license")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDeviceLicense {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private ApplicationLicense license;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private ApplicationDevice device;

    private Date activation_date;
}
