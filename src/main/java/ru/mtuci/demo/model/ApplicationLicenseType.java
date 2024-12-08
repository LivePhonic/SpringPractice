package ru.mtuci.demo.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "license_type")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationLicenseType {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Long defaultDuration;

    private String description;
}

