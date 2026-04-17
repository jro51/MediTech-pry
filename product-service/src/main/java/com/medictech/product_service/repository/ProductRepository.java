package com.medictech.product_service.repository;

import com.medictech.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos con stock disponible
    List<Product> findByStockGreaterThan(Integer stock);

    // Buscar por nombre
    List<Product> findByNameContainingIgnoreCase(String name);
}
