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
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final DeviceServiceImpl deviceServiceImpl;

    public LicenseServiceImpl(LicenseRepository licenseRepository, LicenseTypeServiceImpl licenseTypeService,
                              ProductServiceImpl productService, DeviceLicenseServiceImpl deviceLicenseService,
                              LicenseHistoryServiceImpl licenseHistoryService, UserDetailsServiceImpl userDetailsServiceImpl, DeviceServiceImpl deviceServiceImpl) {
        this.licenseRepository = licenseRepository;
        this.licenseTypeService = licenseTypeService;
        this.productService = productService;
        this.deviceLicenseService = deviceLicenseService;
        this.licenseHistoryService = licenseHistoryService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.deviceServiceImpl = deviceServiceImpl;
    }

    public Optional<ApplicationLicense> getLicenseById(Long id) {
        return licenseRepository.findById(id);
    }

    public Long createLicense(Long productId, Long ownerId, Long licenseTypeId, ApplicationUser user, Long count) {
        ApplicationLicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId).get();
        ApplicationProduct product = productService.getProductById(productId).get();
        ApplicationLicense newLicense = new ApplicationLicense();
        String uid = String.valueOf(UUID.randomUUID());
        while (licenseRepository.findByCode(uid).isPresent()){
            uid = String.valueOf(UUID.randomUUID());
        }
        newLicense.setCode(uid);
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(product.isBlocked());
        newLicense.setDeviceCount(count);
        newLicense.setOwnerId(userDetailsServiceImpl.getUserById(ownerId).get());
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(licenseType.getDescription());

        licenseRepository.save(newLicense);

        licenseHistoryService.createNewRecord("Not activated", "Created new license", user,
                licenseRepository.findTopByOrderByIdDesc().get());

        return licenseRepository.findTopByOrderByIdDesc().get().getId();
    }

    //TODO 1. Посчитать время жизни тикета

    public ApplicationTicket getActiveLicensesForDevice(ApplicationDevice device, String code) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JsonProcessingException {
        List<ApplicationDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicenseById(device);
        List<Long> licenseIds = applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getId() : null)
                .collect(Collectors.toList());
        Optional<ApplicationLicense> applicationLicense = licenseRepository.findByIdInAndCode(licenseIds, code);

        ApplicationTicket ticket = new ApplicationTicket();

        if (applicationLicense.isEmpty()){
            ticket.setInfo("License was not found");
            return ticket;
        }
        ticket = createTicket(applicationLicense.get().getUser(), device, applicationLicense.get(), "200");

        return ticket;
    }


    private String makeSignature(ApplicationTicket ticket) throws NoSuchAlgorithmException, JsonProcessingException, InvalidKeyException, SignatureException {
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

    public ApplicationTicket createTicket(ApplicationUser user, ApplicationDevice device, ApplicationLicense license, String info) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JsonProcessingException {
        ApplicationTicket ticket = new ApplicationTicket();
        ticket.setCurrentDate(new Date());

        if (user != null){
            ticket.setUserId(user.getId());
        }

        if (device != null){
            ticket.setDeviceId(device.getId());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        ticket.setLifetime(calendar.getTime());

        if (license != null){
            ticket.setActivationDate(license.getFirstActivationDate());
            ticket.setExpirationDate(license.getEndingDate());
            ticket.setLicenseBlocked(license.isBlocked());
        }

        ticket.setInfo(info);
        ticket.setDigitalSignature(makeSignature(ticket));

        return ticket;
    }

    public ApplicationTicket activateLicense(String code, ApplicationDevice device, ApplicationUser user) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JsonProcessingException {
        ApplicationTicket ticket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (license.isEmpty()) {
            ticket.setInfo("The license was not found");
            deviceServiceImpl.deleteLastDevice(user);
            return ticket;
        }

        ApplicationLicense newLicense = license.get();
        if (newLicense.isBlocked() || newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate())
                || newLicense.getUser() != null && !Objects.equals(newLicense.getUser().getId(), user.getId()) ||
                Objects.equals(deviceLicenseService.getDeviceCountForLicense(newLicense.getId()), newLicense.getDeviceCount())){
            ticket.setInfo("Activation is not possible");
            deviceServiceImpl.deleteLastDevice(user);
            return ticket;
        }

        if (newLicense.getFirstActivationDate() == null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
            newLicense.setEndingDate(calendar.getTime());
            newLicense.setFirstActivationDate(new Date());
            newLicense.setUser(user);
        }

        deviceLicenseService.createDeviceLicense(newLicense, device);
        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Activated", "Valid license", user,
                newLicense);

        ticket = createTicket(user, device, newLicense, "200");

        return ticket;
    }

    public String updateLicense(Long id, Long ownerId, Long productId, Long typeId, Boolean isBlocked,
                                String description, Long deviceCount){
        Optional<ApplicationLicense> license = getLicenseById(id);
        if (license.isEmpty()) {
            return "License Not Found";
        }

        ApplicationLicense newLicense = license.get();
        newLicense.setCode(String.valueOf(UUID.randomUUID()));
        if (productService.getProductById(productId).isEmpty()){
            return "Product Not Found";
        }

        newLicense.setProduct(productService.getProductById(productId).get());
        if (licenseTypeService.getLicenseTypeById(typeId).isEmpty()){
            return "License Type Not Found";
        }

        newLicense.setLicenseType(licenseTypeService.getLicenseTypeById(typeId).get());
        newLicense.setDuration(licenseTypeService.getLicenseTypeById(typeId).get().getDefaultDuration());
        newLicense.setBlocked(isBlocked);
        newLicense.setOwnerId(userDetailsServiceImpl.getUserById(ownerId).get());
        newLicense.setDescription(description);
        newLicense.setDeviceCount(deviceCount);

        licenseRepository.save(newLicense);

        return "200";
    }

    public ApplicationTicket renewalLicense(String code, ApplicationUser user) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JsonProcessingException {
        ApplicationTicket ticket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (license.isEmpty()) {
            ticket.setInfo("The license key is not valid");
            return ticket;
        }
        ApplicationLicense newLicense = license.get();
        if (newLicense.isBlocked() || newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate())
                || !Objects.equals(newLicense.getOwnerId().getId(), user.getId()) || newLicense.getFirstActivationDate() == null) {
            ticket.setInfo("It is not possible to renew the license");
            return ticket;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newLicense.getEndingDate());
        calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
        newLicense.setEndingDate(calendar.getTime());

        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Renewal", "Valid license", user,
                newLicense);

        ticket = createTicket(user, null, newLicense, "200");

        return ticket;
    }
}