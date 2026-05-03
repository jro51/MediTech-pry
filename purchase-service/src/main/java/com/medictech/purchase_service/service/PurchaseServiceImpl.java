package com.medictech.purchase_service.service;

import com.medictech.purchase_service.client.ProductClient;
import com.medictech.purchase_service.dto.ProductResponse;
import com.medictech.purchase_service.dto.PurchaseEvent;
import com.medictech.purchase_service.dto.PurchaseRequest;
import com.medictech.purchase_service.dto.PurchaseResponse;
import com.medictech.purchase_service.entity.Purchase;
import com.medictech.purchase_service.exception.PurchaseNotFoundException;
import com.medictech.purchase_service.messaging.PurchaseEventPublisher;
import com.medictech.purchase_service.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {
    private final PurchaseEventPublisher eventPublisher;
    private final PurchaseRepository purchaseRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        log.info("Procesando compra para usuario: {}", request.getUserId());

        // 1. Obtenemos los productos
        List<ProductResponse> products = request.getProductIds().stream()
                .map(id -> {
                    log.info("Consultando producto id: {} en product-service", id);
                    return productClient.findById(id);
                })
                .toList();

        // 2. Verificamos stock
        for (ProductResponse product : products) {
            boolean hasStock = productClient.checkStock(product.getId(), 1);
            if (!hasStock) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para: " + product.getName()
                );
            }
        }

        // 3. Calculamos el total
        double total = products.stream()
                .mapToDouble(ProductResponse::getPrice)
                .sum();

        // 4. Guardamos la compra en DB
        Purchase purchase = Purchase.builder()
                .userId(request.getUserId())
                .productIds(request.getProductIds())
                .total(total)
                .purchaseDate(LocalDateTime.now())
                .build();

        Purchase saved = purchaseRepository.save(purchase);

        // 5. Reducimos el stock
        request.getProductIds().forEach(productId -> {
            log.info("Reduciendo stock del producto id: {}", productId);
            productClient.reduceStock(productId, 1);
        });

        log.info("Compra creada en DB con id: {}, total: ${}", saved.getId(), total);

        // 6. Preparamos el evento
        PurchaseEvent event = PurchaseEvent.builder()
                .purchaseId(saved.getId())
                .userId(saved.getUserId())
                .productNames(products.stream().map(ProductResponse::getName).toList())
                .productPrices(products.stream().map(ProductResponse::getPrice).toList())
                .total(total)
                .purchaseDate(saved.getPurchaseDate())
                .build();

        // 7. PUBLICACIÓN SEGURA: Se ejecuta solo después de que la DB confirma el commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    log.info("Commit en BD exitoso. Enviando evento a Kafka...");
                    eventPublisher.publishPurchaseCreated(event);
                } catch (Exception e) {
                    log.error("Fallo al enviar a Kafka post-commit: ", e);
                }
            }
        });

        return toResponse(saved, products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findByUserId(Long userId) {
        log.info("Obteniendo compras del usuario: {}", userId);

        return purchaseRepository.findByUserIdOrderByPurchaseDateDesc(userId)
                .stream()
                .map(purchase -> {
                    List<ProductResponse> products = purchase.getProductIds().stream()
                            .map(productClient::findById)
                            .toList();
                    return toResponse(purchase, products);
                })
                .toList();
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        purchaseRepository.delete(purchase);
        log.info("Compra eliminada con id: {}", id);
    }

    private PurchaseResponse toResponse(Purchase purchase, List<ProductResponse> products) {
        List<PurchaseResponse.PurchaseItemResponse> items = products.stream()
                .map(p -> PurchaseResponse.PurchaseItemResponse.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .price(p.getPrice())
                        .build())
                .toList();

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .userId(purchase.getUserId())
                .products(items)
                .total(purchase.getTotal())
                .purchaseDate(purchase.getPurchaseDate())
                .build();
    }
}