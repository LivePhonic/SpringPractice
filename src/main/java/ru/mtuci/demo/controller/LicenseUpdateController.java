package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.LicenseUpdateRequest;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;

import java.util.Objects;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseUpdateController {

    private final LicenseServiceImpl licenseService;

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseUpdateRequest request) {
        try {

            String res = licenseService.updateLicense(request.getId(), request.getOwnerId(), request.getProductId(),
                    request.getTypeId(), request.getIsBlocked(), request.getDescription(), request.getDeviceCount());
            if (!Objects.equals(res, "OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(res);
            }

            return ResponseEntity.status(HttpStatus.OK).body("License updated successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
