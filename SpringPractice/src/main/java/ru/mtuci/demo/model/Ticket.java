package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Ticket {
    private String deviceId; // Идентификатор устройства
    private long lifetime; // Время жизни тикета в миллисекундах
    private LocalDateTime activationDate; // Дата активации
    private LocalDateTime expirationDate; // Дата истечения
    private boolean licenseBlocked; // Статус блокировки лицензии
    private String digitalSignature; // Цифровая подпись
}
