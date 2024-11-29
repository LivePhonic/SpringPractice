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
}