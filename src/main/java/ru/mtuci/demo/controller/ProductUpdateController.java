package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.ProductUpdateRequest;
import ru.mtuci.demo.service.impl.ProductServiceImpl;

import java.util.Objects;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductUpdateController {

    private final ProductServiceImpl productService;

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('modification')")
    public ResponseEntity<?> updateProduct(@RequestBody ProductUpdateRequest request) {
        try {

            String res = productService.upadteProduct(request.getProductId(), request.getName(), request.getIsBlocked());
            if (!Objects.equals(res, "OK")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(res);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Product updated successfully.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Oops, something went wrong....");
        }
    }
}