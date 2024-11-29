package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<ApplicationLicense, Long> {
    Optional<ApplicationLicense> findById(Long id);
    Optional<ApplicationLicense> findTopByOrderByIdDesc();
    Optional<ApplicationLicense> findByCode(String code);
    List<ApplicationLicense> findAllByIdIn(List<Long> ids);
}
