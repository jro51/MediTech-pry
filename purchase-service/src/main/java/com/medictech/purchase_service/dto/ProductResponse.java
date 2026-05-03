package com.medictech.purchase_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private String description;
    private String imageSrc;
}
