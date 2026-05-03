package com.medictech.purchase_service.controller;

import com.medictech.purchase_service.dto.PurchaseRequest;
import com.medictech.purchase_service.dto.PurchaseResponse;
import com.medictech.purchase_service.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/buy")
    public ResponseEntity<PurchaseResponse> createPurchase(
            @RequestBody PurchaseRequest request,
            // El gateway inyecta estos headers desde el JWT
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // Usamos el userId del JWT — más seguro que confiar en el body
        if (userId != null) {
            request.setUserId(Long.parseLong(userId));
        }

        log.info("Solicitud de compra recibida para usuario: {}", request.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(purchaseService.createPurchase(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PurchaseResponse>> findByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String tokenUserId) {

        // Verificamos que el usuario solo vea sus propias compras
        if (tokenUserId != null && !tokenUserId.equals(String.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(purchaseService.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }
}
