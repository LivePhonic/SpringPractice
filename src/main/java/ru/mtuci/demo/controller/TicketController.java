package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.Ticket;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final LicenseServiceImpl licenseService;

    @GetMapping("/{licenseId}")
    public ResponseEntity<?> getTicketByLicenseId(@PathVariable UUID licenseId) {
        try {
            Optional<ApplicationLicense> optionalLicense = licenseService.getLicenseById(licenseId);
            if (!optionalLicense.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("License not found for ID: " + licenseId);
            }

            ApplicationLicense license = optionalLicense.get();
            Ticket ticket = new Ticket(
                    license.getDevice_count(),
                    3600000,
                    license.getFirst_activation_date(),
                    license.getEnding_date(),
                    license.isBlocked(),
                    "digital_signature"
            );


            return ResponseEntity.ok(ticket);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to retrieve ticket");
        }
    }
}
