package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "device")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDevice {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String mac_address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
}