package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.repository.LicenseRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class LicenseServiceImpl {
    private final LicenseRepository licenseRepository;

    public LicenseServiceImpl(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public Optional<ApplicationLicense> getLicenseById(UUID id) {
        return licenseRepository.findById(id);
    }
}
