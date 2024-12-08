package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.service.impl.ProductServiceImpl;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductCreateController {

    private final ProductServiceImpl productService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> createProduct(@RequestBody ProductCreateRequest request) {
        try {

            Long id = productService.createProduct(request.getName(), request.getIsBlocked());

            return ResponseEntity.status(HttpStatus.OK).body("Product created successfully.\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}
