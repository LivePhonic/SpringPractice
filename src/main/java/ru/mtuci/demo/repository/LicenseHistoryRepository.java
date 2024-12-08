package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationLicenseHistory;

import java.util.Optional;

public interface LicenseHistoryRepository extends JpaRepository<ApplicationLicenseHistory, Long> {
    Optional<ApplicationLicenseHistory> findById(Long id);
}
