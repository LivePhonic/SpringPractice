package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repository.LicenseRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.security.*;
import java.util.stream.Collectors;

@Service
public class LicenseServiceImpl {
    private final LicenseRepository licenseRepository;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final ProductServiceImpl productService;
    private final DeviceLicenseServiceImpl deviceLicenseService;
    private final LicenseHistoryServiceImpl licenseHistoryService;

    public LicenseServiceImpl(LicenseRepository licenseRepository, LicenseTypeServiceImpl licenseTypeService, LicenseTypeServiceImpl licenseTypeService1, ProductServiceImpl productService, DeviceLicenseServiceImpl deviceLicenseService, LicenseHistoryServiceImpl licenseHistoryService) {
        this.licenseRepository = licenseRepository;
        this.licenseTypeService = licenseTypeService1;
        this.productService = productService;
        this.deviceLicenseService = deviceLicenseService;
        this.licenseHistoryService = licenseHistoryService;
    }

    public Optional<ApplicationLicense> getLicenseById(Long id) {
        return licenseRepository.findById(id);
    }

    public ApplicationLicenseHistory createLicense(Long productId, Long ownerId, Long licenseTypeId) {
        ApplicationLicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId).get();
        ApplicationProduct product = productService.getProductById(productId).get();
        ApplicationLicense newLicense = new ApplicationLicense();
        newLicense.setCode(String.valueOf(UUID.randomUUID()));
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(product.isBlocked());
        newLicense.setDeviceCount(0);
        newLicense.setOwnerId(ownerId);
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(licenseType.getDescription());

        licenseRepository.save(newLicense);

        return licenseHistoryService.createNewRecord("Not activated", "Created new license", null,
                licenseRepository.findTopByOrderByIdDesc().get());
    }

    @SneakyThrows
    public List<ApplicationTicket> getActiveLicensesForDevice(ApplicationDevice device) {
        List<ApplicationDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicenseById(device);
        List<Long> licenseIds = applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getId() : null)
                .collect(Collectors.toList());
        List<ApplicationLicense> applicationLicenseList = licenseRepository.findAllByIdIn(licenseIds);

        return applicationLicenseList.stream()
                .map(license -> convertToTicket(license, device.getId()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private ApplicationTicket convertToTicket(ApplicationLicense license, Long deviceId) {
        ApplicationTicket ticket = new ApplicationTicket();
        ticket.setCurrentDate(new Date());
        ticket.setUserId(license.getOwnerId());
        ticket.setDeviceId(deviceId);
        ticket.setLifetime("One hour");
        ticket.setActivationDate(license.getFirstActivationDate());
        ticket.setExpirationDate(license.getEndingDate());
        ticket.setLicenseBlocked(license.isBlocked());
        ticket.setDigitalSignature(makeSignature(ticket));
        return ticket;
    }

    @SneakyThrows
    private String makeSignature(ApplicationTicket ticket) {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        ObjectMapper objectMapper = new ObjectMapper();
        String res = objectMapper.writeValueAsString(ticket);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(res.getBytes());

        return Base64.getEncoder().encodeToString(signature.sign());
    }

    @SneakyThrows
    public ApplicationTicket activateLicense(String code, ApplicationDevice device, ApplicationUser user) {
        ApplicationTicket applicationTicket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (!license.isPresent()) {
            applicationTicket.setInfo("The license was not found");
            return applicationTicket;
        }

        ApplicationLicense newLicense = license.get();
        if (newLicense.isBlocked() || newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate())){
            applicationTicket.setInfo("Activation is not possible");
            return applicationTicket;
        }

        if (newLicense.getFirstActivationDate() == null){
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH, newLicense.getDuration());
            newLicense.setEndingDate(calendar.getTime());
            newLicense.setFirstActivationDate(new Date());
        }

        newLicense.setUser(user);
        newLicense.setDeviceCount(newLicense.getDeviceCount() + 1);

        deviceLicenseService.createDeviceLicense(newLicense, device);
        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Activated", "Valid license", user,
                newLicense);

        applicationTicket.setCurrentDate(new Date());
        applicationTicket.setUserId(user.getId());
        applicationTicket.setDeviceId(device.getId());
        applicationTicket.setLifetime("One hour");
        applicationTicket.setActivationDate(newLicense.getFirstActivationDate());
        applicationTicket.setExpirationDate(newLicense.getEndingDate());
        applicationTicket.setLicenseBlocked(newLicense.isBlocked());
        applicationTicket.setInfo("The license has been successfully activated");
        applicationTicket.setDigitalSignature(makeSignature(applicationTicket));

        return applicationTicket;
    }

    public ApplicationTicket renewalLicense(String code, ApplicationUser user) {
        ApplicationTicket ticket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (!license.isPresent()) {
            ticket.setInfo("The license key is not valid");
            return ticket;
        }
        ApplicationLicense newLicense = license.get();
        if (newLicense.isBlocked() || newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate())
                || newLicense.getOwnerId() != user.getId()) {
            ticket.setInfo("It is not possible to renew the license");
            return ticket;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newLicense.getEndingDate());
        calendar.add(Calendar.DAY_OF_MONTH, newLicense.getDuration());
        newLicense.setEndingDate(calendar.getTime());

        newLicense.setUser(user);
        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Renewal", "Valid license", user,
                newLicense);

        ticket.setCurrentDate(new Date());
        ticket.setUserId(user.getId());
        ticket.setLifetime("One hour");
        ticket.setActivationDate(newLicense.getFirstActivationDate());
        ticket.setExpirationDate(newLicense.getEndingDate());
        ticket.setLicenseBlocked(newLicense.isBlocked());
        ticket.setInfo("The license has been successfully renewed");
        ticket.setDigitalSignature(makeSignature(ticket));

        return ticket;
    }
}
