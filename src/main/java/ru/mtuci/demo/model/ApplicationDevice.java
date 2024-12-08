package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDevice {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String macAddress;

    @ManyToOne
    @JoinColumn(name = "userId")
    private ApplicationUser user;
}