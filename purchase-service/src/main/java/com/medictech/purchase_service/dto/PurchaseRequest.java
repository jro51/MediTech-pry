package com.medictech.purchase_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    private Long userId;
    private List<Long> productIds;
}
