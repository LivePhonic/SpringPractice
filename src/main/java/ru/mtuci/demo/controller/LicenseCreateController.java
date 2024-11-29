package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repository.LicenseRepository;
import ru.mtuci.demo.service.impl.*;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseCreateController {

    private final ProductServiceImpl productService;
    private final UserDetailsServiceImpl userService;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final LicenseServiceImpl licenseService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseCreateRequest request) {
        try {
            Long productId = request.getProductId();
            Long ownerId = request.getOwnerId();
            Long licenseTypeId = request.getLicenseTypeId();

            if (productService.getProductById(productId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product with given ID does not exist.");
            }

            if (userService.getUserById(ownerId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Owner with given ID does not exist.");
            }

            if (licenseTypeService.getLicenseTypeById(licenseTypeId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type with given ID does not exist.");
            }

            licenseService.createLicense(productId, ownerId, licenseTypeId);

            return ResponseEntity.status(HttpStatus.CREATED).body("License created successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}