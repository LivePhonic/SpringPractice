package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.DeviceRepository;

import java.util.Optional;

@Service
public class DeviceServiceImpl {
    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Optional<ApplicationDevice> getDeviceByInfo(ApplicationUser user, String mac_address, String name) {
        return deviceRepository.findByUserAndMacAddressAndName(user, mac_address, name);
    }

    public void deleteLastDevice(ApplicationUser user) {
        Optional<ApplicationDevice> lastDevice = deviceRepository.findTopByUserOrderByIdDesc(user);
        lastDevice.ifPresent(deviceRepository::delete);
    }

    public ApplicationDevice registerOrUpdateDevice(String mac, String name, ApplicationUser user) {
        ApplicationDevice newDevice = getDeviceByInfo(user, mac, name)
                .orElse(new ApplicationDevice());

        newDevice.setName(name);
        newDevice.setMacAddress(mac);
        newDevice.setUser(user);

        return deviceRepository.save(newDevice);
    }
}
