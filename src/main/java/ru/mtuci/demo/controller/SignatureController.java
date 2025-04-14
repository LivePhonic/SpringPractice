package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.impl.SignatureAuditServiceImpl;
import ru.mtuci.demo.service.impl.SignatureServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailsServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.ByteArrayOutputStream;

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
                    request.getFileType(), request.getOffsetStart(), request.getOffsetEnd(), user.getId(), request.getStatus());

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

    @GetMapping(value = "/manifest", produces = MediaType.MULTIPART_MIXED_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getSignatureManifest() {
        try{
            List<ApplicationSignature> signatures = signatureService.getAllActualSignatures(SignatureStatus.ACTUAL);

            int signatureCount = signatures.size();
            List<String> signatureEntries = new ArrayList<>();
            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();

            for (ApplicationSignature signature : signatures) {
                String entry = signature.getId() + ":" + signature.getDigitalSignature();
                signatureEntries.add(entry);

                writeSignatureDataToStream(dataOutputStream, signature);
            }

            byte[] manifest = createManifest(signatureCount, signatureEntries);
            byte[] data = dataOutputStream.toByteArray();

            return buildMultipartResponse(manifest, data);
        }
        catch (IOException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<MultiValueMap<String, Object>> buildMultipartResponse(byte [] manifest, byte[] data) {
        ByteArrayResource manifestRes = new ByteArrayResource(manifest) {
            @Override
            public String getFilename() {
                return "manifest.bin";
            }
        };

        ByteArrayResource dataRes = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return "data.bin";
            }
        };

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("manifest", new HttpEntity<>(manifestRes, createHeaders("manifest.bin")));
        parts.add("data", new HttpEntity<>(dataRes, createHeaders("data.bin")));

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("multipart/mixed")).body(parts);
    }

    private HttpHeaders createHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return headers;
    }

    private void writeUuidBE(ByteArrayOutputStream baos, UUID uuid) {
        writeLongBE(baos, uuid.getMostSignificantBits());
        writeLongBE(baos, uuid.getLeastSignificantBits());
    }

    private void writeLongBE(ByteArrayOutputStream baos, long value) {
        baos.write((byte) ((value >> 56) & 0xFF));
        baos.write((byte) ((value >> 48) & 0xFF));
        baos.write((byte) ((value >> 40) & 0xFF));
        baos.write((byte) ((value >> 32) & 0xFF));
        baos.write((byte) ((value >> 24) & 0xFF));
        baos.write((byte) ((value >> 16) & 0xFF));
        baos.write((byte) ((value >> 8) & 0xFF));
        baos.write((byte) (value & 0xFF));
    }

    private void writeIntBE(ByteArrayOutputStream baos, int value) {
        baos.write((byte) ((value >> 24) & 0xFF));
        baos.write((byte) ((value >> 16) & 0xFF));
        baos.write((byte) ((value >> 8) & 0xFF));
        baos.write((byte) (value & 0xFF));
    }

    private void writeStringBE(ByteArrayOutputStream baos, String value) {
        byte[] bytes = value.getBytes();
        writeIntBE(baos, bytes.length);
        baos.write(bytes, 0, bytes.length);
    }

    private void writeSignatureDataToStream(ByteArrayOutputStream baos, ApplicationSignature signature) throws IOException {
        writeUuidBE(baos, signature.getId());
        baos.write('|');

        writeStringBE(baos, signature.getThreatName());
        baos.write('|');

        writeStringBE(baos, signature.getFirstBytes());
        baos.write('|');

        writeStringBE(baos, signature.getRemainderHash());
        baos.write('|');

        writeIntBE(baos, signature.getRemainderLength());
        baos.write('|');

        writeStringBE(baos, signature.getFileType());
        baos.write('|');

        writeIntBE(baos, signature.getOffsetStart());
        baos.write('|');

        writeIntBE(baos, signature.getOffsetEnd());
        byte[] fixedBytes = new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33 };
        baos.write(fixedBytes);
    }

    private byte[] createManifest(int signatureCount, List<String> signatureEntries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writeIntBE(baos, signatureCount);

        for (String entry : signatureEntries) {
            writeStringBE(baos, entry);
        }

        String manifestHash = signatureService.makeHash(baos.toString(StandardCharsets.UTF_8));
        writeStringBE(baos, signatureService.makeSignature(manifestHash));

        return baos.toByteArray();
    }
}
