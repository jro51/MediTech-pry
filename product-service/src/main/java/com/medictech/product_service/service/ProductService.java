package com.medictech.product_service.service;

import com.medictech.product_service.dto.ProductRequest;
import com.medictech.product_service.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    // Metodo para verificar stock
    // antes de procesar una compra
    boolean checkStock(Long productId, Integer quantity);

    // Reduce el stock cuando se confirma una compra
    void reduceStock(Long productId, Integer quantity);
}
