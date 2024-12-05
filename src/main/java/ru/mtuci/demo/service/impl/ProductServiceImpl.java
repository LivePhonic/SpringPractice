package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationProduct;
import ru.mtuci.demo.repository.ProductRepository;

import java.util.Optional;

//TODO: 1. Маловато методов. В других классах тоже. Расширьте

@Service
public class ProductServiceImpl {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<ApplicationProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }
}
