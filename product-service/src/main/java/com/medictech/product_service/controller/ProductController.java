package com.medictech.product_service.controller;

import com.medictech.product_service.dto.ProductRequest;
import com.medictech.product_service.dto.ProductResponse;
import com.medictech.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody ProductRequest request,
            // El gateway ya validó el JWT e inyectó este header
            // Ya no dependemos del frontend para saber el rol
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para crear productos");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ProductRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para editar productos");
        }

        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para eliminar productos");
        }

        productService.delete(id);
        return ResponseEntity.ok("Producto eliminado correctamente");
    }

    // Endpoint interno — solo lo llama purchase-service, nunca el frontend
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(productService.checkStock(id, quantity));
    }

    // Endpoint interno — purchase-service lo llama al confirmar una compra
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Void> reduceStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}
