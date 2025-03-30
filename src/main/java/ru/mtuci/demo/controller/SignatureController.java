package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.impl.SignatureAuditServiceImpl;
import ru.mtuci.demo.service.impl.SignatureServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailsServiceImpl;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureServiceImpl signatureService;
    private final SignatureAuditServiceImpl signatureAuditService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/actual")
    public ResponseEntity<?> getAllActualSignatures() {
        try {
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);
            return ResponseEntity.ok(signatures);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/updated-after")
    public ResponseEntity<?> getSignaturesUpdatedAfter(@RequestBody SignaturesUpdatedAfterRequest request) {
        try {
            List<ApplicationSignature> signatures = signatureService.getSignaturesUpdatedAfter(request.getSince());
            return ResponseEntity.ok(Objects.requireNonNullElse(signatures, "Invalid date"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/by-guids")
    public ResponseEntity<?> getSignaturesByGuids(@RequestBody SignaturesByGuidsRequest request) {
        try {
            List<ApplicationSignature> signatures = signatureService.getSignaturesByGuids(request.getGuids());
            return ResponseEntity.ok(signatures);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> addSignature(@RequestBody SignaturesAddRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            ApplicationSignature signature = signatureService.addSignature(request.getThreatName(), request.getFirstBytes(),
                    request.getRemainderLength(), request.getFileType(), request.getOffsetStart(),
                    request.getOffsetEnd(), user.getId());

            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> deleteSignature(@RequestBody SignaturesDeleteRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            String result = signatureService.deleteSignature(request.getSignatureUUID(), user.getId());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateSignature(@RequestBody SignaturesUpdateRequest request, HttpServletRequest req) {
        try {
            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            ApplicationSignature signature = signatureService.updateSignature(request.getSignatureId(),
                    request.getThreatName(), request.getFirstBytes(), request.getRemainderLength(),
                    request.getFileType(), request.getOffsetStart(), request.getOffsetEnd(), user.getId());

            return ResponseEntity.ok(Objects.requireNonNullElse(signature, "Signature not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @PostMapping("/by-status")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> getSignaturesByStatus(@RequestBody SignaturesByStatusRequest request) {
        try {
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(request.getStatus());
            return ResponseEntity.ok(signatures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> signatureAudit() {
        try {
            List<ApplicationSignatureAudit> auditRecords = signatureAuditService.getAllAuditRecords();
            return ResponseEntity.ok(auditRecords);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
