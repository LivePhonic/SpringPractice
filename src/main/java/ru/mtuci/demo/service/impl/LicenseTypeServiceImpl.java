package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicenseType;
import ru.mtuci.demo.repository.LicenseTypeRepository;

import java.util.Optional;

@Service
public class LicenseTypeServiceImpl {
    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseTypeServiceImpl(LicenseTypeRepository licenseTypeRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
    }

    public Optional<ApplicationLicenseType> getLicenseTypeById(Long id) {
        return licenseTypeRepository.findById(id);
    }

    public Long createLicenseType(Long duration, String description, String name) {
        ApplicationLicenseType licenseType = new ApplicationLicenseType();
        licenseType.setDescription(description);
        licenseType.setName(name);
        licenseType.setDefaultDuration(duration);
        licenseTypeRepository.save(licenseType);
        return licenseTypeRepository.findTopByOrderByIdDesc().get().getId();
    }

    public String upadteLicenseType(Long id, Long duration, String description, String name) {
        Optional<ApplicationLicenseType> licenseType = getLicenseTypeById(id);
        if (licenseType.isEmpty()) {
            return "License Type Not Found";
        }

        ApplicationLicenseType newlicenseType = licenseType.get();
        newlicenseType.setName(name);
        newlicenseType.setDefaultDuration(duration);
        newlicenseType.setDescription(description);
        licenseTypeRepository.save(newlicenseType);
        return "OK";
    }
}