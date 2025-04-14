package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationSignature;
import ru.mtuci.demo.model.SignatureStatus;
import ru.mtuci.demo.repository.SignatureAuditRepository;
import ru.mtuci.demo.repository.SignatureHistoryRepository;
import ru.mtuci.demo.repository.SignatureRepository;

import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SignatureServiceImpl {

    private final SignatureRepository signatureRepository;
    private final SignatureHistoryServiceImpl signatureHistoryService;
    private final SignatureAuditServiceImpl signatureAuditService;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private Date lastCheckTime;

    @Autowired
    public SignatureServiceImpl(SignatureRepository signatureRepository, SignatureHistoryRepository signatureHistoryRepository, SignatureAuditRepository signatureAuditRepository, SignatureHistoryServiceImpl signatureHistoryService, SignatureAuditServiceImpl signatureAuditService, PrivateKey privateKey, PublicKey publicKey) {
        this.signatureRepository = signatureRepository;
        this.signatureHistoryService = signatureHistoryService;
        this.signatureAuditService = signatureAuditService;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public List<ApplicationSignature> getAllActualSignatures(SignatureStatus status) {
        return signatureRepository.findByStatus(status);
    }

    public List<ApplicationSignature> getSignaturesUpdatedAfter(String since) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date parsedDate = sdf.parse(since);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            calendar.add(Calendar.DATE, 1);
            Date nextDay = calendar.getTime();

            return signatureRepository.findByUpdatedAtAfter(nextDay);
        }catch (Exception e){
            return null;
        }
    }

    public List<ApplicationSignature> getSignaturesByGuids(List<UUID> guids) {
        return signatureRepository.findByIdIn(guids);
    }

    public ApplicationSignature addSignature(String threatName, String firstBytes, Integer remainderLength,
                                             String fileType, Integer offsetStart, Integer offsetEnd, Long userId) throws JsonProcessingException {
        String hash = makeHash(firstBytes.substring(firstBytes.length() - remainderLength * 2));

        ApplicationSignature signature = new ApplicationSignature();
        signature.setId(UUID.randomUUID());
        signature.setThreatName(threatName);
        signature.setFirstBytes(firstBytes);
        signature.setRemainderLength(remainderLength);
        signature.setFileType(fileType);
        signature.setOffsetStart(offsetStart);
        signature.setOffsetEnd(offsetEnd);
        signature.setRemainderHash(hash);
        Date date = new Date();
        signature.setUpdatedAt(date);
        signature.setStatus(SignatureStatus.ACTUAL);

        ObjectMapper objectMapper = new ObjectMapper();
        String digitalSignature = makeSignature(objectMapper.writeValueAsString(signature));

        signature.setDigitalSignature(digitalSignature);

        signatureRepository.save(signature);

        signatureHistoryService.createSignatureHistory(signature);

        signatureAuditService.createSignatureAudit(signature.getId(), userId, "CREATED", date, "All");

        return signature;
    }

    public String deleteSignature(UUID signatureUUID, Long userId){
        Optional<ApplicationSignature> signature = signatureRepository.findById(signatureUUID);

        if (signature.isEmpty()){
            return "Wrong UUId";
        }

        ApplicationSignature deletedSignature = signature.get();
        if (deletedSignature.getStatus().equals(SignatureStatus.DELETED)){
            return "Signature already deleted";
        }

        deletedSignature.setStatus(SignatureStatus.DELETED);
        signatureRepository.save(deletedSignature);
        signatureAuditService.createSignatureAudit(signatureUUID, userId, "DELETED", new Date(), "Status");
        return "The signature has been successfully marked as DELETED";
    }

    public ApplicationSignature updateSignature(UUID signatureUUID, String threatName, String firstBytes, Integer remainderLength,
                                                String fileType, Integer offsetStart, Integer offsetEnd, Long userId, SignatureStatus status) throws JsonProcessingException {
        Optional<ApplicationSignature> signature = signatureRepository.findById(signatureUUID);

        String changedFields = "";

        if (signature.isEmpty()){
            return null;
        }

        ApplicationSignature updateSignature = signature.get();

        signatureHistoryService.createSignatureHistory(updateSignature);

        if (firstBytes != null){
            updateSignature.setFirstBytes(firstBytes);
            changedFields = changedFields + "firstBytes, ";
        }
        if (remainderLength != null){
            updateSignature.setRemainderLength(remainderLength);
            changedFields = changedFields + "remainderLength, ";
        }

        String hash = makeHash(updateSignature
                .getFirstBytes()
                .substring(updateSignature.getFirstBytes().length() - updateSignature.getRemainderLength() * 2));

        updateSignature.setRemainderHash(hash);

        if (threatName != null){
            updateSignature.setThreatName(threatName);
            changedFields = changedFields + "threatName, ";
        }
        if (fileType != null){
            updateSignature.setFileType(fileType);
            changedFields = changedFields + "fileType, ";
        }
        if (offsetStart != null){
            updateSignature.setOffsetStart(offsetStart);
            changedFields = changedFields + "offsetStart, ";
        }
        if (offsetEnd != null){
            updateSignature.setOffsetEnd(offsetEnd);
            changedFields = changedFields + "offsetEnd, ";
        }
        if (status == SignatureStatus.ACTUAL || status == SignatureStatus.DELETED || status == SignatureStatus.CORRUPTED){
            updateSignature.setStatus(status);
            changedFields = changedFields + "status, ";
        }

        Date date = new Date();
        updateSignature.setUpdatedAt(date);

        ObjectMapper objectMapper = new ObjectMapper();

        updateSignature.setDigitalSignature(makeSignature(objectMapper.writeValueAsString(signature)));

        signatureRepository.save(updateSignature);

        changedFields = changedFields.substring(0, changedFields.length() - 2);

        signatureAuditService.createSignatureAudit(signatureUUID, userId, "UPDATED", date, changedFields);

        return updateSignature;
    }

    public String makeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public String makeSignature(String res) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            return "Something went wrong. The signature is not valid";
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkSignature() throws JsonProcessingException {
        List<ApplicationSignature> signatures = signatureRepository.findByUpdatedAtAfter(lastCheckTime);
        for (ApplicationSignature signature : signatures) {
            String originalDigitalSignature = signature.getDigitalSignature();
            signature.setDigitalSignature(null);
            ObjectMapper objectMapper = new ObjectMapper();

            String computedSignature = makeSignature(objectMapper.writeValueAsString(signature));
            if (!computedSignature.equals(originalDigitalSignature) && signature.getStatus() == SignatureStatus.ACTUAL) {
                signatureHistoryService.createSignatureHistory(signature);
                signature.setStatus(SignatureStatus.CORRUPTED);
                signatureRepository.save(signature);
                signatureAuditService.createSignatureAudit(signature.getId(), null,
                        "ERROR", new Date(), "Status");
            }
        }

        lastCheckTime = new Date();
    }
}
