package ru.mtuci.demo.service.impl;

import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationProduct;
import ru.mtuci.demo.repository.ProductRepository;

import java.util.Optional;

@Service
public class ProductServiceImpl {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<ApplicationProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public String upadteProduct(Long id, String name, Boolean isBlocked) {
        Optional<ApplicationProduct> product = getProductById(id);
        if (product.isEmpty()) {
            return "Product Not Found";
        }

        ApplicationProduct newProduct = product.get();
        newProduct.setName(name);
        newProduct.setBlocked(isBlocked);
        productRepository.save(newProduct);
        return "OK";
    }

    public Long createProduct(String name, Boolean isBlocked){
        ApplicationProduct product = new ApplicationProduct();
        product.setBlocked(isBlocked);
        product.setName(name);
        productRepository.save(product);
        return productRepository.findTopByOrderByIdDesc().get().getId();
    }
}
