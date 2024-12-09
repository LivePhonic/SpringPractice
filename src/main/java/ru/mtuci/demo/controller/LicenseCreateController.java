package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.impl.*;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseCreateController {

    private final ProductServiceImpl productService;
    private final UserDetailsServiceImpl userService;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final LicenseServiceImpl licenseService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    //TODO: 1. Неверно используются Http статусы. Они относятся к состоянию запроса, а не к вашей логике. Лучше отправить 200ОК

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseCreateRequest request, HttpServletRequest req) {
        try {
            Long productId = request.getProductId();
            Long ownerId = request.getOwnerId();
            Long licenseTypeId = request.getLicenseTypeId();

            if (productService.getProductById(productId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Product with given ID does not exist.");
            }

            if (productService.getProductById(productId).get().isBlocked()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("This product is not available.");
            }

            if (userService.getUserById(ownerId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Owner with given ID does not exist.");
            }

            if (licenseTypeService.getLicenseTypeById(licenseTypeId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("License type with given ID does not exist.");
            }

            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            Long id = licenseService.createLicense(productId, ownerId, licenseTypeId, user, request.getCount());

            return ResponseEntity.status(HttpStatus.OK).body("License created successfully.\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}