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
public class PurchaseResponse {
    private Long id;
    private Long userId;
    private List<PurchaseItemResponse> products;
    private Double total;
    private LocalDateTime purchaseDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemResponse {
        private Long productId;
        private String productName;
        private Double price;
    }
}
