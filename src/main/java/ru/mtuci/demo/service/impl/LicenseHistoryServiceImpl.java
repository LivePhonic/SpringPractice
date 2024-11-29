package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationLicenseHistory;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.LicenseHistoryRepository;
import ru.mtuci.demo.repository.LicenseRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class LicenseHistoryServiceImpl {
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final LicenseRepository licenseRepository;

    public LicenseHistoryServiceImpl(LicenseHistoryRepository licenseHistoryRepository, LicenseRepository licenseRepository) {
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.licenseRepository = licenseRepository;
    }

    public Optional<ApplicationLicenseHistory> getLicenseHistoryById(Long id) {
        return licenseHistoryRepository.findById(id);
    }

    public ApplicationLicenseHistory createNewRecord(String status, String description, ApplicationUser user, ApplicationLicense license){
        ApplicationLicenseHistory newHistory = new ApplicationLicenseHistory();
        newHistory.setLicense(license);
        newHistory.setStatus(status);
        newHistory.setChangeDate(new Date());
        newHistory.setDescription(description);
        newHistory.setUser(user);

        return licenseHistoryRepository.save(newHistory);
    }
}
