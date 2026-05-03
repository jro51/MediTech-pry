package com.medictech.purchase_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseEvent {
    private Long purchaseId;
    private Long userId;
    private List<String> productNames;
    private List<Double> productPrices;
    private Double total;
    private LocalDateTime purchaseDate;
}
