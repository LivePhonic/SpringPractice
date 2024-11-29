package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicenseType;

import java.util.Optional;

public interface LicenseTypeRepository extends JpaRepository<ApplicationLicenseType, Long> {
    Optional<ApplicationLicenseType> findById(Long id);
}