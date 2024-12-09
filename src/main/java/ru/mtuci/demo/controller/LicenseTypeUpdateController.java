package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.LicenseTypeUpdateRequest;
import ru.mtuci.demo.service.impl.LicenseTypeServiceImpl;

import java.util.Objects;

@RestController
@RequestMapping("/api/type")
@RequiredArgsConstructor
public class LicenseTypeUpdateController {

    private final LicenseTypeServiceImpl licenseTypeService;

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateLicenseType(@RequestBody LicenseTypeUpdateRequest request) {
        try {

            String res = licenseTypeService.upadteLicenseType(request.getId(), request.getDuration(),
                    request.getDescription(), request.getName());
            if (!Objects.equals(res, "OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(res);
            }

            return ResponseEntity.status(HttpStatus.OK).body("New type added successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
