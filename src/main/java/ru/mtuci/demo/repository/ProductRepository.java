package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationProduct;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ApplicationProduct, Long> {
    Optional<ApplicationProduct> findById(Long id);
}
