package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationLicenseHistory;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.LicenseHistoryRepository;

import java.util.Date;

@Service
public class LicenseHistoryServiceImpl {
    private final LicenseHistoryRepository licenseHistoryRepository;

    public LicenseHistoryServiceImpl(LicenseHistoryRepository licenseHistoryRepository) {
        this.licenseHistoryRepository = licenseHistoryRepository;
    }

    public ApplicationLicenseHistory createNewRecord(String status, String description,
                                                     ApplicationUser user, ApplicationLicense license){
        ApplicationLicenseHistory newHistory = new ApplicationLicenseHistory();
        newHistory.setLicense(license);
        newHistory.setStatus(status);
        newHistory.setChangeDate(new Date());
        newHistory.setDescription(description);
        newHistory.setUser(user);

        return licenseHistoryRepository.save(newHistory);
    }
}
