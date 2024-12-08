package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.repository.DeviceLicenseRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
@Service
public class DeviceLicenseServiceImpl {
    private final DeviceLicenseRepository deviceLicenseRepository;

    public DeviceLicenseServiceImpl(DeviceLicenseRepository deviceLicenseRepository) {
        this.deviceLicenseRepository = deviceLicenseRepository;
    }

    public Optional<ApplicationDeviceLicense> getDeviceById(Long id) {
        return deviceLicenseRepository.findById(id);
    }

    public Long getDeviceCountForLicense(Long licenseId) {
        return deviceLicenseRepository.countByLicenseId(licenseId);
    }

    public List<ApplicationDeviceLicense> getAllLicenseById(ApplicationDevice device) {
        return deviceLicenseRepository.findByDeviceId(device.getId());
    }

    public ApplicationDeviceLicense createDeviceLicense(ApplicationLicense license, ApplicationDevice device) {
        ApplicationDeviceLicense newLicense = new ApplicationDeviceLicense();
        newLicense.setLicense(license);
        newLicense.setDevice(device);
        newLicense.setActivationDate(new Date());
        return deviceLicenseRepository.save(newLicense);
    }
}
