package com.medictech.purchase_service.service;

import com.medictech.purchase_service.dto.PurchaseRequest;
import com.medictech.purchase_service.dto.PurchaseResponse;

import java.util.List;

public interface PurchaseService {
    PurchaseResponse createPurchase(PurchaseRequest request);
    List<PurchaseResponse> findByUserId(Long userId);
    void deletePurchase(Long id);
}
