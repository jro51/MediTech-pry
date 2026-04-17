package com.medictech.product_service.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Long id) {
        super("Producto no encontrado con id: " + id);
    }
}
