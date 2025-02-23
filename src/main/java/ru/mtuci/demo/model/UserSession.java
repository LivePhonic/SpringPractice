package ru.mtuci.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private Long deviceId;

    private String accessToken;

    private String refreshToken;

    private Date accessTokenExpiry;

    private Date refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    @Version
    private Long version;
}

