package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationSignatureHistory;

public interface SignatureHistoryRepository extends JpaRepository<ApplicationSignatureHistory, Long> {
}
