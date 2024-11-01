package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicense;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<ApplicationLicense, UUID> {
    Optional<ApplicationLicense> findById(UUID id);
}
